package pl.pomykalskimateusz.recruitmenttask.coupon;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pomykalskimateusz.recruitmenttask.jooq.entity.tables.Coupon;
import pl.pomykalskimateusz.recruitmenttask.jooq.entity.tables.CouponUsage;
import pl.pomykalskimateusz.recruitmenttask.model.CouponData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static pl.pomykalskimateusz.recruitmenttask.jooq.entity.Tables.COUPON;
import static pl.pomykalskimateusz.recruitmenttask.jooq.entity.tables.CouponUsage.COUPON_USAGE;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CouponReadRepository {
  static Field<Integer> CURRENT_USAGE_FIELD = DSL.field("has_assortment", Integer.class);
  static Field<Boolean> COUPON_EXISTS_FIELD = DSL.field("exists", Boolean.class);
  static Field<Integer> COUPON_TOTAL_USAGE_FIELD = DSL.field("total_usage", Integer.class);
  static Field<Integer> COUPON_USER_USAGE_FIELD = DSL.field("user_usage", Integer.class);

  static Coupon COUPON_ALIAS = COUPON.as("coupon_alias");
  static CouponUsage COUPON_USAGE_ALIAS = COUPON_USAGE.as("coupon_usage_alias");
  static CouponUsage USER_COUPON_USAGE_ALIAS = COUPON_USAGE.as("user_coupon_usage_alias");

  static List<Field<?>> COUPON_USAGE_SELECT_FIELDS = List.of(
    COUPON_ALIAS.ID, COUPON_ALIAS.COUNTRY, COUPON_ALIAS.USAGE_LIMIT, COUPON_ALIAS.ID.isNotNull().as(COUPON_EXISTS_FIELD), DSL.count(COUPON_USAGE_ALIAS.ID).as(COUPON_TOTAL_USAGE_FIELD), DSL.count(USER_COUPON_USAGE_ALIAS.ID).as(COUPON_USER_USAGE_FIELD)
  );
  static List<Field<?>> COUPON_DATA_SELECT_FIELDS = List.of(
    COUPON.ID, COUPON.CODE, COUPON.USAGE_LIMIT, COUPON.COUNTRY, COUPON.CREATED_DATE_TIMESTAMP, DSL.count(COUPON_USAGE.ID).as(CURRENT_USAGE_FIELD)
  );

  DSLContext dslContext;

  @Transactional
  public List<CouponData> findAll() {
    return dslContext.select(COUPON_DATA_SELECT_FIELDS)
      .from(COUPON)
      .leftJoin(COUPON_USAGE).on(COUPON.ID.eq(COUPON_USAGE.COUPON_ID))
      .groupBy(COUPON.ID)
      .fetch()
      .map(this::buildCouponData);
  }

  @Transactional
  public boolean existsByCode(String code) {
    var query = dslContext.selectOne().from(COUPON).where(COUPON.CODE_NORMALIZED.eq(code.toUpperCase()));
    return dslContext.fetchExists(query);
  }

  public record CouponUsageData(UUID couponId, String country, boolean couponExists, int usageLimit, int totalUsage, int userUsage) {}

  @Transactional
  public Optional<CouponUsageData> findCouponUsageByCode(String code, UUID userId) {
    return dslContext
      .select(COUPON_USAGE_SELECT_FIELDS)
      .from(COUPON_ALIAS)
      .leftJoin(COUPON_USAGE_ALIAS).on(COUPON_USAGE_ALIAS.COUPON_ID.eq(COUPON_ALIAS.ID))
      .leftJoin(USER_COUPON_USAGE_ALIAS).on(USER_COUPON_USAGE_ALIAS.COUPON_ID.eq(COUPON_ALIAS.ID).and(USER_COUPON_USAGE_ALIAS.USER_ID.eq(userId)))
      .where(COUPON_ALIAS.CODE_NORMALIZED.eq(code.toUpperCase()))
      .groupBy(COUPON_ALIAS.ID)
      .fetchOptional()
      .map(this::buildCouponUsageData);
  }

  private CouponUsageData buildCouponUsageData(Record dbRecord) {
    var couponId = dbRecord.get(COUPON_ALIAS.ID);
    var country = dbRecord.get(COUPON_ALIAS.COUNTRY);
    var exists = dbRecord.get(COUPON_EXISTS_FIELD);
    var totalUsage = dbRecord.get(COUPON_TOTAL_USAGE_FIELD);
    var userUsage = dbRecord.get(COUPON_USER_USAGE_FIELD);
    var usageLimit = dbRecord.get(COUPON_ALIAS.USAGE_LIMIT);

    return new CouponUsageData(couponId, country, exists, usageLimit, totalUsage, userUsage);
  }

  private CouponData buildCouponData(Record dbRecord) {
    return new CouponData()
      .id(dbRecord.getValue(COUPON.ID))
      .code(dbRecord.getValue(COUPON.CODE))
      .usageLimit(dbRecord.getValue(COUPON.USAGE_LIMIT))
      .countryCode(dbRecord.getValue(COUPON.COUNTRY))
      .currentUsage(dbRecord.get(CURRENT_USAGE_FIELD))
      .createDate(dbRecord.get(COUPON.CREATED_DATE_TIMESTAMP));
  }
}
