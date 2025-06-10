package pl.pomykalskimateusz.recruitmenttask.coupon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pl.pomykalskimateusz.recruitmenttask.exception.BadRequestException;
import pl.pomykalskimateusz.recruitmenttask.exception.ResourceNotFoundException;
import pl.pomykalskimateusz.recruitmenttask.model.CreateCouponBody;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CouponValidateServiceTest {
  private final CouponValidateService couponValidateService = new CouponValidateService();

  @ParameterizedTest
  @MethodSource("invalidCodeValues")
  void shouldThrowExceptionForInvalidCodeInCreateCouponBody(String code) {
    // GIVEN create-coupon body with invalid code
    var createCouponBody = new CreateCouponBody()
      .code(code)
      .usageLimit(1)
      .countryCode("PL");

    // WHEN validating body with invalid country code THEN BadRequestException should be thrown
    assertThrows(BadRequestException.class, () -> couponValidateService.validateCreateCoupon(createCouponBody));
  }

  @ParameterizedTest
  @MethodSource("invalidCountryCodeValues")
  void shouldThrowExceptionForInvalidCountryCodeInCreateCouponBody(String countryCode) {
    // GIVEN create-coupon body with invalid country code
    var createCouponBody = new CreateCouponBody()
      .code("test")
      .usageLimit(1)
      .countryCode(countryCode);

    // WHEN validating body with invalid country code THEN BadRequestException should be thrown
    assertThrows(BadRequestException.class, () -> couponValidateService.validateCreateCoupon(createCouponBody));
  }

  @ParameterizedTest
  @MethodSource("invalidUsageLimitValues")
  void shouldThrowExceptionForInvalidUsageLimitInCreateCouponBody(Integer usageLimit) {
    // GIVEN create-coupon body with invalid usage limit
    var createCouponBody = new CreateCouponBody()
      .code("test")
      .usageLimit(usageLimit)
      .countryCode("PL");

    // WHEN validating body with invalid usage limit THEN BadRequestException should be thrown
    assertThrows(BadRequestException.class, () -> couponValidateService.validateCreateCoupon(createCouponBody));
  }

  @Test
  void shouldNotThrowExceptionForValidCreateCouponBody() {
    // GIVEN create-coupon body with valid parameters
    var createCouponBody = new CreateCouponBody()
      .code("test")
      .usageLimit(10)
      .countryCode("PL");

    // WHEN validating body with invalid usage limit THEN BadRequestException should be thrown
    assertDoesNotThrow(() -> couponValidateService.validateCreateCoupon(createCouponBody));
  }

  @ParameterizedTest
  @MethodSource("invalidCodeValues")
  void shouldThrowExceptionForInvalidCodeInRegisterCoupon(String code) {
    // GIVEN user-id and invalid code
    var userId = UUID.randomUUID();

    // WHEN validating register coupon parameters with invalid code THEN BadRequestException should be thrown
    assertThrows(BadRequestException.class, () -> couponValidateService.validateRegisterCoupon(userId, code));
  }


  @Test
  void shouldThrowExceptionForMissingUserIdInRegisterCoupon() {
    // GIVEN missing user-id and valid code
    var code = "test";

    // WHEN validating register coupon parameters with invalid code THEN BadRequestException should be thrown
    assertThrows(BadRequestException.class, () -> couponValidateService.validateRegisterCoupon(null, code));
  }

  @Test
  void shouldThrowExceptionForNotEqualCountryCodeInCouponUsage() {
    // GIVEN coupon usage data and invalid country code
    var couponUsage = new CouponReadRepository.CouponUsageData(UUID.randomUUID(), "PL", true, 10, 5, 0);

    // WHEN validating coupon usage with invalid country code THEN ResourceNotFoundException should be thrown
    assertThrows(ResourceNotFoundException.class, () -> couponValidateService.validateCouponUsage(couponUsage, "test", "US"));
  }

  @Test
  void shouldThrowExceptionForExceededUsageLimitInCouponUsage() {
    // GIVEN coupon usage data with exceeded usage limit
    var couponUsage = new CouponReadRepository.CouponUsageData(UUID.randomUUID(), "PL", true, 10, 10, 0);

    // WHEN validating coupon usage with exceeded usage limit THEN BadRequestException should be thrown
    assertThrows(BadRequestException.class, () -> couponValidateService.validateCouponUsage(couponUsage, "test", "PL"));
  }

  @Test
  void shouldThrowExceptionForExceededUsageLimitByUserInCouponUsage() {
    // GIVEN coupon usage data with exceeded usage limit by user
    var couponUsage = new CouponReadRepository.CouponUsageData(UUID.randomUUID(), "PL", true, 10, 5, 1);

    // WHEN validating coupon usage with exceeded usage limit by user THEN BadRequestException should be thrown
    assertThrows(BadRequestException.class, () -> couponValidateService.validateCouponUsage(couponUsage, "test", "PL"));
  }

  @Test
  void shouldNotThrowExceptionForValidCouponUsage() {
    // GIVEN coupon usage data with exceeded usage limit by user
    var couponUsage = new CouponReadRepository.CouponUsageData(UUID.randomUUID(), "PL", true, 10, 5, 0);

    // WHEN validating coupon usage with exceeded usage limit by user THEN BadRequestException should be thrown
    assertDoesNotThrow(() -> couponValidateService.validateCouponUsage(couponUsage, "test", "PL"));
  }

  private static Stream<Integer> invalidUsageLimitValues() {
    return Stream.of(null, 0, -1);
  }

  private static Stream<String> invalidCodeValues() {
    return Stream.of("", null);
  }

  private static Stream<String> invalidCountryCodeValues() {
    return Stream.of(null, "", "QQ");
  }
}
