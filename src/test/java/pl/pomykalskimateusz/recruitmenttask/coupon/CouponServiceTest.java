package pl.pomykalskimateusz.recruitmenttask.coupon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomykalskimateusz.recruitmenttask.DatabaseContainer;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CouponServiceTest extends DatabaseContainer {
  @Autowired
  CouponService couponService;

  @AfterEach
  void afterEach() {
    super.cleanDatabase("public", false);
  }
}
