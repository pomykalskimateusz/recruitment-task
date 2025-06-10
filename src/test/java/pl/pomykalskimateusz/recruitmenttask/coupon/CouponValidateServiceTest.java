package pl.pomykalskimateusz.recruitmenttask.coupon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pl.pomykalskimateusz.recruitmenttask.exception.BadRequestException;
import pl.pomykalskimateusz.recruitmenttask.model.CreateCouponBody;

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
