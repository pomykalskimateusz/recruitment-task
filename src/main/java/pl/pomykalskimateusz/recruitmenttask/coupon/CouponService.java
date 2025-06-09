package pl.pomykalskimateusz.recruitmenttask.coupon;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CouponService {
  public boolean dummyMethod() {
    return true;
  }
}
