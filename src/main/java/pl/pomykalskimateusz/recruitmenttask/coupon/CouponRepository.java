package pl.pomykalskimateusz.recruitmenttask.coupon;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pomykalskimateusz.recruitmenttask.jooq.entity.tables.Coupon;
import pl.pomykalskimateusz.recruitmenttask.jooq.entity.tables.CouponUsage;
import pl.pomykalskimateusz.recruitmenttask.model.BasicCouponData;
import pl.pomykalskimateusz.recruitmenttask.model.CouponData;
import pl.pomykalskimateusz.recruitmenttask.model.CreateCouponBody;

import static pl.pomykalskimateusz.recruitmenttask.jooq.entity.Tables.COUPON;
import static pl.pomykalskimateusz.recruitmenttask.jooq.entity.tables.CouponUsage.COUPON_USAGE;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CouponRepository {
  static String CURRENT_USAGE_FIELD = "current_usage";
  static Coupon COUPON_ALIAS = COUPON.as("coupon_alias");
  static CouponUsage COUPON_USAGE_ALIAS = COUPON_USAGE.as("coupon_usage_alias");
  static CouponUsage USER_COUPON_USAGE_ALIAS = COUPON_USAGE.as("user_coupon_usage_alias");

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
  public List<CouponData> findAll() {
    return dslContext.select(COUPON.ID, COUPON.CODE, COUPON.USAGE_LIMIT, COUPON.COUNTRY, COUPON.CREATED_DATE_TIMESTAMP, DSL.count(COUPON_USAGE.ID).as(CURRENT_USAGE_FIELD)
      )
      .from(COUPON)
      .leftJoin(COUPON_USAGE).on(COUPON.ID.eq(COUPON_USAGE.COUPON_ID))
      .groupBy(COUPON.ID)
      .fetch()
      .map(this::buildCouponData);
  }

  @Transactional
  public Optional<UUID> findIdByCode(String code) {
    return dslContext.select(COUPON.ID)
      .from(COUPON)
      .where(COUPON.CODE_NORMALIZED.eq(code.toUpperCase()))
      .fetchOptional(COUPON.ID);
  }

  public record CouponUsageData(UUID couponId, String country, boolean couponExists, int usageLimit, int totalUsage, int userUsage) {}

  @Transactional
  public Optional<CouponUsageData> findCouponUsageByCode(String code, UUID userId) {
    return dslContext
      .select(
        COUPON_ALIAS.ID,
        COUPON_ALIAS.COUNTRY,
        COUPON_ALIAS.USAGE_LIMIT,
        COUPON_ALIAS.ID.isNotNull().as("exists"),
        DSL.count(COUPON_USAGE_ALIAS.ID).as("total_usage"),
        DSL.count(USER_COUPON_USAGE_ALIAS.ID).as("user_usage")
      )
      .from(COUPON_ALIAS)
      .leftJoin(COUPON_USAGE_ALIAS).on(COUPON_USAGE_ALIAS.COUPON_ID.eq(COUPON_ALIAS.ID))
      .leftJoin(USER_COUPON_USAGE_ALIAS).on(USER_COUPON_USAGE_ALIAS.COUPON_ID.eq(COUPON_ALIAS.ID).and(USER_COUPON_USAGE_ALIAS.USER_ID.eq(userId)))
      .where(COUPON_ALIAS.CODE_NORMALIZED.eq(code.toUpperCase()))
      .groupBy(COUPON_ALIAS.ID)
      .fetchOptional()
      .map(dbRecord -> {
        var couponId = dbRecord.get(COUPON_ALIAS.ID);
        var country = dbRecord.get(COUPON_ALIAS.COUNTRY);
        var exists = dbRecord.get("exists", Boolean.class);
        var totalUsage = dbRecord.get("total_usage", Integer.class);
        var userUsage = dbRecord.get("user_usage", Integer.class);
        var usageLimit = dbRecord.get(COUPON_ALIAS.USAGE_LIMIT);

        return new CouponUsageData(couponId, country, exists, usageLimit, totalUsage, userUsage);
      });
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

  @Transactional
  public boolean existsByCode(String code) {
    return dslContext.fetchExists(
      dslContext.selectOne()
        .from(COUPON)
        .where(COUPON.CODE_NORMALIZED.eq(code.toUpperCase()))
    );
  }

  private BasicCouponData buildBasicCouponData(Record dbRecord) {
    return new BasicCouponData()
      .id(dbRecord.getValue(COUPON.ID))
      .code(dbRecord.getValue(COUPON.CODE))
      .usageLimit(dbRecord.getValue(COUPON.USAGE_LIMIT))
      .countryCode(dbRecord.getValue(COUPON.COUNTRY))
      .createDate(dbRecord.get(COUPON.CREATED_DATE_TIMESTAMP));
  }

  private CouponData buildCouponData(Record dbRecord) {
    return new CouponData()
      .id(dbRecord.getValue(COUPON.ID))
      .code(dbRecord.getValue(COUPON.CODE))
      .usageLimit(dbRecord.getValue(COUPON.USAGE_LIMIT))
      .countryCode(dbRecord.getValue(COUPON.COUNTRY))
      .currentUsage(dbRecord.get(CURRENT_USAGE_FIELD, Integer.class))
      .createDate(dbRecord.get(COUPON.CREATED_DATE_TIMESTAMP));
  }
}
