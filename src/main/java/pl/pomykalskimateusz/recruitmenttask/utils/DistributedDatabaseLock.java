package pl.pomykalskimateusz.recruitmenttask.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jooq.DSLContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DistributedDatabaseLock {
  public static void lockCouponCreation(DSLContext dslContext, String couponCode) {
    long hash = ("coupon_creation" + couponCode.toUpperCase()).hashCode();
    dslContext.execute("SELECT pg_advisory_xact_lock(" + hash + ")");
  }

  public static void lockCouponRegistration(DSLContext dslContext, String couponCode) {
    long hash = ("coupon_registration" + couponCode.toUpperCase()).hashCode();
    dslContext.execute("SELECT pg_advisory_xact_lock(" + hash + ")");
  }
}
