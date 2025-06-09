package pl.pomykalskimateusz.recruitmenttask.coupon;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import pl.pomykalskimateusz.recruitmenttask.exception.BadRequestException;
import pl.pomykalskimateusz.recruitmenttask.model.CreateCouponBody;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CouponValidateService {
  static Set<String> ISO_COUNTRIES = Arrays.stream(Locale.getISOCountries()).collect(Collectors.toSet());

  public void validateCreateCoupon(CreateCouponBody createCouponBody) {
    if(isCountryCodeNotValid(createCouponBody.getCountryCode())) {
      throw new BadRequestException(String.format("Invalid country code: %s", createCouponBody.getCountryCode()));
    }
    if(isUsageLimitNotValid(createCouponBody.getUsageLimit())) {
      throw new BadRequestException(String.format("Invalid usage limit: %s, value should be positive", createCouponBody.getUsageLimit()));
    }
  }

  public void validateCouponUsage(CouponReadRepository.CouponUsageData couponUsage, String code, String countryCode) {
    if(!countryCode.equals(couponUsage.country())) {
      throw new BadRequestException(String.format("Not found coupon code: %s for country: %s", code, countryCode));
    }
    if(couponUsage.usageLimit() == couponUsage.totalUsage()) {
      throw new BadRequestException(String.format("Code not available. Usage limit exceeded for code: %s", code));       //todo replace with NotFoundException
    }
    if(couponUsage.userUsage() > 0) {
      throw new BadRequestException(String.format("User usage limit exceeded for code: %s and country: %s", code, countryCode));       //todo replace with NotFoundException
    }
  }

  private boolean isCountryCodeNotValid(String countryCode) {
    return !ISO_COUNTRIES.contains(countryCode);
  }

  private boolean isUsageLimitNotValid(int usageLimit) {
    return usageLimit <= 0;
  }
}
