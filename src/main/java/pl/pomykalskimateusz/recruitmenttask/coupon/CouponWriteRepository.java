package pl.pomykalskimateusz.recruitmenttask.coupon;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pomykalskimateusz.recruitmenttask.model.BasicCouponData;
import pl.pomykalskimateusz.recruitmenttask.model.CreateCouponBody;

import static pl.pomykalskimateusz.recruitmenttask.jooq.entity.Tables.COUPON;
import static pl.pomykalskimateusz.recruitmenttask.jooq.entity.tables.CouponUsage.COUPON_USAGE;

import java.util.Optional;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CouponWriteRepository {
  DSLContext dslContext;

  @Transactional
  public Optional<BasicCouponData> insert(CreateCouponBody body) {
    return dslContext.insertInto(COUPON)
      .set(COUPON.ID, UUID.randomUUID())
      .set(COUPON.CODE, body.getCode())
      .set(COUPON.USAGE_LIMIT, body.getUsageLimit())
      .set(COUPON.COUNTRY, body.getCountryCode().toUpperCase())
      .set(COUPON.VERSION, 1)
      .returningResult(COUPON.asterisk())
      .fetchOptional()
      .map(this::buildBasicCouponData);
  }

  @Transactional
  public void insertCouponUsage(UUID userId, UUID couponId) {
    dslContext.insertInto(COUPON_USAGE)
      .set(COUPON_USAGE.ID, UUID.randomUUID())
      .set(COUPON_USAGE.COUPON_ID, couponId)
      .set(COUPON_USAGE.USER_ID, userId)
      .set(COUPON.VERSION, 1)
      .execute();
  }

  private BasicCouponData buildBasicCouponData(Record dbRecord) {
    return new BasicCouponData()
      .id(dbRecord.getValue(COUPON.ID))
      .code(dbRecord.getValue(COUPON.CODE))
      .usageLimit(dbRecord.getValue(COUPON.USAGE_LIMIT))
      .countryCode(dbRecord.getValue(COUPON.COUNTRY))
      .createDate(dbRecord.get(COUPON.CREATED_DATE_TIMESTAMP));
  }
}
