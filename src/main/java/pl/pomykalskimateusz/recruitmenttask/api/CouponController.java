package pl.pomykalskimateusz.recruitmenttask.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.pomykalskimateusz.recruitmenttask.coupon.CouponService;
import pl.pomykalskimateusz.recruitmenttask.exception.BadRequestException;
import pl.pomykalskimateusz.recruitmenttask.model.BasicCouponData;
import pl.pomykalskimateusz.recruitmenttask.model.CouponData;
import pl.pomykalskimateusz.recruitmenttask.model.CreateCouponBody;
import pl.pomykalskimateusz.recruitmenttask.model.RegisterCouponBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CouponController implements CouponsApi {
  CouponService couponService;

  @Override
  public ResponseEntity<List<CouponData>> getAllCoupons() {
    return ResponseEntity.ok(couponService.fetchCoupons());
  }

  @Override
  public ResponseEntity<BasicCouponData> createCoupon(CreateCouponBody body) {
    return couponService.createCoupon(body)
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.status(500).build());
  }

  @Override
  public ResponseEntity<Void> registerUserCoupon(String code, RegisterCouponBody body) {
    couponService.registerCoupon(body.getUserId(), code, getClientIpAddress());
    return ResponseEntity.ok().build();
  }

  private String getClientIpAddress() {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) throw new BadRequestException("Incorrect headers, missing request attributes.");
    HttpServletRequest request = attributes.getRequest();

    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader != null && !xfHeader.isBlank()) {
      return xfHeader.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
