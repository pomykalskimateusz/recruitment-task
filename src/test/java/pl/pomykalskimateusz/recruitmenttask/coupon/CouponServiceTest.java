package pl.pomykalskimateusz.recruitmenttask.coupon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import pl.pomykalskimateusz.recruitmenttask.DatabaseContainer;
import pl.pomykalskimateusz.recruitmenttask.exception.BadRequestException;
import pl.pomykalskimateusz.recruitmenttask.exception.ResourceNotFoundException;
import pl.pomykalskimateusz.recruitmenttask.localization.LocalizationService;
import pl.pomykalskimateusz.recruitmenttask.model.CreateCouponBody;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class CouponServiceTest extends DatabaseContainer {
  @Autowired
  CouponService couponService;

  @MockBean
  LocalizationService localizationService;

  @AfterEach
  void afterEach() {
    super.cleanDatabase("public", false);
  }

  @Test
  void shouldThrowExceptionWhenCouponAlreadyExistsDuringCreation() {
    // GIVEN already created coupon with code "test"
    var createCouponBody = new CreateCouponBody()
      .code("test")
      .countryCode("PL")
      .usageLimit(1);

    couponService.createCoupon(createCouponBody);

    // WHEN creating coupon with code that is already in database THEN BadRequestException should be thrown
    assertThrows(BadRequestException.class, () -> couponService.createCoupon(createCouponBody));
  }

  @Test
  void shouldThrowExceptionWhenCouponAlreadyExistsWithDifferentCaseSensitivityDuringCreation() {
    // GIVEN already created coupon with code "test"
    var createCouponBody = new CreateCouponBody()
      .code("test")
      .countryCode("PL")
      .usageLimit(1);

    couponService.createCoupon(createCouponBody);

    // WHEN creating coupon with the same code with different case sensitivity THEN BadRequestException should be thrown
    var createDuplicatedCouponBody = new CreateCouponBody()
      .code("TeSt")
      .countryCode("PL")
      .usageLimit(1);
    assertThrows(BadRequestException.class, () -> couponService.createCoupon(createDuplicatedCouponBody));
  }

  @Test
  void shouldCreateCoupon() {
    // GIVEN create coupon body with valid parameters
    var createCouponBody = new CreateCouponBody()
      .code("test")
      .countryCode("PL")
      .usageLimit(1);

    // WHEN creating new coupon
    couponService.createCoupon(createCouponBody);

    // THEN coupon should be correctly saved in database
    var coupons = couponService.fetchCoupons();

    assertEquals(1, coupons.size());
    assertEquals("test", coupons.getFirst().getCode());
    assertEquals("PL", coupons.getFirst().getCountryCode());
    assertEquals(1, coupons.getFirst().getUsageLimit());
    assertEquals(0, coupons.getFirst().getCurrentUsage());
  }

  @Test
  void shouldThrowExceptionWhenCountryCodeNotFoundDuringRegistration() {
    // GIVEN register coupon parameters with not-existing ip-address
    var userId = UUID.randomUUID();
    var ipAddress = "127.0.0.1";
    var code = "test";

    when(localizationService.getCountryCodeByIp(ipAddress)).thenReturn(Optional.empty());

    // WHEN registering coupon with not-existing country code by ip-address THEN ResourceNotFoundException should be thrown
    assertThrows(ResourceNotFoundException.class, () -> couponService.registerCoupon(userId, code, ipAddress));
  }

  @Test
  void shouldThrowExceptionWhenCouponNotFoundDuringRegistration() {
    // GIVEN register coupon parameters with not-existing code
    var userId = UUID.randomUUID();
    var ipAddress = "127.0.0.1";
    var code = "test";

    when(localizationService.getCountryCodeByIp(ipAddress)).thenReturn(Optional.of("PL"));

    // WHEN registering coupon with not-existing coupon code THEN ResourceNotFoundException should be thrown
    assertThrows(ResourceNotFoundException.class, () -> couponService.registerCoupon(userId, code, ipAddress));
  }

  @Test
  void shouldThrowExceptionWhenUsageLimitExceededDuringRegistration() {
    // GIVEN existing coupon along with registered one
    var userId1 = UUID.randomUUID();
    var userId2 = UUID.randomUUID();
    var ipAddress = "127.0.0.1";
    var code = "test";
    var countryCode = "PL";
    var createCouponBody = new CreateCouponBody()
      .code(code)
      .countryCode(countryCode)
      .usageLimit(1);

    when(localizationService.getCountryCodeByIp(ipAddress)).thenReturn(Optional.of(countryCode));
    couponService.createCoupon(createCouponBody);
    couponService.registerCoupon(userId1, code, ipAddress);

    // WHEN registering coupon by user2 THEN BadRequestException should be thrown
    assertThrows(BadRequestException.class, () -> couponService.registerCoupon(userId2, code, ipAddress));
  }

  @Test
  void shouldThrowExceptionWhenUserUsageLimitExceededDuringRegistration() {
    // GIVEN existing coupon along with registered one
    var userId = UUID.randomUUID();
    var ipAddress = "127.0.0.1";
    var code = "test";
    var countryCode = "PL";
    var createCouponBody = new CreateCouponBody()
      .code(code)
      .countryCode(countryCode)
      .usageLimit(2);

    when(localizationService.getCountryCodeByIp(ipAddress)).thenReturn(Optional.of(countryCode));
    couponService.createCoupon(createCouponBody);
    couponService.registerCoupon(userId, code, ipAddress);

    // WHEN registering coupon again by the same user THEN BadRequestException should be thrown
    assertThrows(BadRequestException.class, () -> couponService.registerCoupon(userId, code, ipAddress));
  }

  @Test
  void shouldThrowExceptionWhenCouponCountryDiffersFromUserCountryCode() {
    // GIVEN existing coupon along with registered one
    var userId = UUID.randomUUID();
    var ipAddress = "127.0.0.1";
    var code = "test";
    var createCouponBody = new CreateCouponBody()
      .code(code)
      .countryCode("US")
      .usageLimit(1);

    when(localizationService.getCountryCodeByIp(ipAddress)).thenReturn(Optional.of("PL"));
    couponService.createCoupon(createCouponBody);

    // WHEN registering coupon with different country-code THEN ResourceNotFoundException should be thrown
    assertThrows(ResourceNotFoundException.class, () -> couponService.registerCoupon(userId, code, ipAddress));
  }

  @Test
  void shouldRegisterCoupon() {
    // GIVEN register coupon parameters and existing coupon
    var userId = UUID.randomUUID();
    var ipAddress = "127.0.0.1";
    var code = "test";
    var countryCode = "PL";
    var createCouponBody = new CreateCouponBody()
      .code(code)
      .countryCode(countryCode)
      .usageLimit(1);

    when(localizationService.getCountryCodeByIp(ipAddress)).thenReturn(Optional.of(countryCode));
    couponService.createCoupon(createCouponBody);

    // WHEN registering coupon
    couponService.registerCoupon(userId, code, ipAddress);

    // THEN coupon should be registered and current usage of coupon should be 1
    var coupons = couponService.fetchCoupons();

    assertEquals(1, coupons.size());
    assertEquals(1, coupons.getFirst().getCurrentUsage());
  }

  @Test
  void shouldRegisterCouponByDifferentUsers() {
    // GIVEN register coupon parameters and existing coupon
    var userId1 = UUID.randomUUID();
    var userId2 = UUID.randomUUID();
    var ipAddress = "127.0.0.1";
    var code = "test";
    var countryCode = "PL";
    var createCouponBody = new CreateCouponBody()
      .code(code)
      .countryCode(countryCode)
      .usageLimit(10);

    when(localizationService.getCountryCodeByIp(ipAddress)).thenReturn(Optional.of(countryCode));
    couponService.createCoupon(createCouponBody);

    // WHEN registering coupon by two different users
    couponService.registerCoupon(userId1, code, ipAddress);
    couponService.registerCoupon(userId2, code, ipAddress);

    // THEN coupon should be registered and current usage of coupon should be 2
    var coupons = couponService.fetchCoupons();

    assertEquals(1, coupons.size());
    assertEquals(2, coupons.getFirst().getCurrentUsage());
  }

  @Test
  void shouldRegisterCouponWhenCodeSensitivityIsDifferentDuringRegistration() {
    // GIVEN register coupon parameters and existing coupon
    var userId = UUID.randomUUID();
    var ipAddress = "127.0.0.1";
    var code = "test";
    var countryCode = "PL";
    var createCouponBody = new CreateCouponBody()
      .code(code)
      .countryCode(countryCode)
      .usageLimit(1);

    when(localizationService.getCountryCodeByIp(ipAddress)).thenReturn(Optional.of(countryCode));
    couponService.createCoupon(createCouponBody);

    // WHEN registering coupon
    couponService.registerCoupon(userId, "TeSt", ipAddress);

    // THEN coupon should be registered and current usage of coupon should be 1
    var coupons = couponService.fetchCoupons();

    assertEquals(1, coupons.size());
    assertEquals(1, coupons.getFirst().getCurrentUsage());
  }

  @Test
  void shouldRegisterCouponWhenCountryCodeSensitivityIsDifferentDuringRegistration() {
    // GIVEN register coupon parameters and existing coupon
    var userId = UUID.randomUUID();
    var ipAddress = "127.0.0.1";
    var code = "test";
    var countryCode = "PL";
    var createCouponBody = new CreateCouponBody()
      .code(code)
      .countryCode("pl")
      .usageLimit(1);

    when(localizationService.getCountryCodeByIp(ipAddress)).thenReturn(Optional.of(countryCode));
    couponService.createCoupon(createCouponBody);

    // WHEN registering coupon
    couponService.registerCoupon(userId, code, ipAddress);

    // THEN coupon should be registered and current usage of coupon should be 1
    var coupons = couponService.fetchCoupons();

    assertEquals(1, coupons.size());
    assertEquals(1, coupons.getFirst().getCurrentUsage());
  }
}
