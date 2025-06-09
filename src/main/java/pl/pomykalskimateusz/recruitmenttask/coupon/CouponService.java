package pl.pomykalskimateusz.recruitmenttask.coupon;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pomykalskimateusz.recruitmenttask.exception.BadRequestException;
import pl.pomykalskimateusz.recruitmenttask.localization.LocalizationService;
import pl.pomykalskimateusz.recruitmenttask.model.*;
import pl.pomykalskimateusz.recruitmenttask.utils.DistributedDatabaseLock;

import java.util.*;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CouponService {
  DSLContext dslContext;
  CouponRepository couponRepository;
  CouponValidateService couponValidateService;
  LocalizationService localizationService;

  @Transactional
  public List<CouponData> fetchCoupons() {
    return couponRepository.findAll();
  }

  @Transactional
  public Optional<BasicCouponData> createCoupon(CreateCouponBody createCouponBody) {
    couponValidateService.validateCreateCouponBody(createCouponBody);
    DistributedDatabaseLock.lockCouponCreation(dslContext, createCouponBody.getCode());

    if(couponRepository.existsByCode(createCouponBody.getCode())) {
      throw new BadRequestException(String.format("Duplicated coupon code found: %s", createCouponBody.getCode()));
    }

    return couponRepository.insert(createCouponBody);
  }

  @Transactional
  public void registerCoupon(UUID userId, String code, String ipAddress) {
    var optionalCountryCode = localizationService.getCountryCodeByIp(ipAddress);
    if(optionalCountryCode.isEmpty()) {
      throw new BadRequestException(String.format("Not found coupon code: %s", code));
    }

    DistributedDatabaseLock.lockCouponRegistration(dslContext, code);
    couponRepository.insertCouponUsage(userId, fetchCouponUsageByCode(userId, code, optionalCountryCode.get()).couponId());
  }

  private CouponRepository.CouponUsageData fetchCouponUsageByCode(UUID userId, String code, String countryCode) {
    var optionalCouponUsage = couponRepository.findCouponUsageByCode(code, userId);
    if(optionalCouponUsage.isEmpty()) {
      throw new BadRequestException(String.format("Not found coupon code: %s", code));       //todo replace with NotFoundException
    }

    var couponUsage = optionalCouponUsage.get();

    if(!countryCode.equals(couponUsage.country())) {
      throw new BadRequestException(String.format("Not found coupon code: %s for country: %s", code, countryCode));
    }
    if(couponUsage.usageLimit() == couponUsage.totalUsage()) {
      throw new BadRequestException(String.format("Code not available. Usage limit exceeded for code: %s", code));       //todo replace with NotFoundException
    }
    if(couponUsage.userUsage() > 0) {
      throw new BadRequestException(String.format("User usage limit exceeded for code: %s and country: %s", code, countryCode));       //todo replace with NotFoundException
    }

    return couponUsage;
  }
}
