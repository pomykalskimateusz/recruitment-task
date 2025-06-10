package pl.pomykalskimateusz.recruitmenttask.coupon;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pomykalskimateusz.recruitmenttask.exception.BadRequestException;
import pl.pomykalskimateusz.recruitmenttask.exception.ResourceNotFoundException;
import pl.pomykalskimateusz.recruitmenttask.localization.LocalizationService;
import pl.pomykalskimateusz.recruitmenttask.model.BasicCouponData;
import pl.pomykalskimateusz.recruitmenttask.model.CouponData;
import pl.pomykalskimateusz.recruitmenttask.model.CreateCouponBody;
import pl.pomykalskimateusz.recruitmenttask.utils.DistributedDatabaseLock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CouponService {
  DSLContext dslContext;
  CouponWriteRepository couponWriteRepository;
  CouponReadRepository couponReadRepository;
  CouponValidateService couponValidateService;
  LocalizationService localizationService;

  @Transactional
  public List<CouponData> fetchCoupons() {
    return couponReadRepository.findAll();
  }

  @Transactional
  public Optional<BasicCouponData> createCoupon(CreateCouponBody createCouponBody) {
    couponValidateService.validateCreateCoupon(createCouponBody);
    DistributedDatabaseLock.lockCouponCreation(dslContext, createCouponBody.getCode());

    if(couponReadRepository.existsByCode(createCouponBody.getCode())) {
      throw new BadRequestException(String.format("Duplicated coupon code found: %s", createCouponBody.getCode()));
    }

    return couponWriteRepository.insert(createCouponBody);
  }

  @Transactional
  public void registerCoupon(UUID userId, String code, String ipAddress) {
    var optionalCountryCode = localizationService.getCountryCodeByIp(ipAddress);
    if(optionalCountryCode.isEmpty()) {
      throw new ResourceNotFoundException(String.format("Not found country code for ip address: %s", ipAddress));
    }

    DistributedDatabaseLock.lockCouponRegistration(dslContext, code);

    couponWriteRepository.insertCouponUsage(userId, fetchCouponUsageByCode(userId, code, optionalCountryCode.get()).couponId());
  }

  private CouponReadRepository.CouponUsageData fetchCouponUsageByCode(UUID userId, String code, String countryCode) {
    var optionalCouponUsage = couponReadRepository.findCouponUsageByCode(code, userId);
    if(optionalCouponUsage.isEmpty()) {
      throw new ResourceNotFoundException(String.format("Not found coupon code: %s", code));
    }

    couponValidateService.validateCouponUsage(optionalCouponUsage.get(), code, countryCode);

    return optionalCouponUsage.get();
  }
}
