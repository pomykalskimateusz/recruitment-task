package pl.pomykalskimateusz.recruitmenttask.localization;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.pomykalskimateusz.recruitmenttask.utils.ThrowableSupplier;

import java.util.Optional;

@Service
public class LocalizationService {
  private final RestTemplate restTemplate = new RestTemplate();

  private record LocalizationData(String countryCode) {}

  public Optional<String> getCountryCodeByIp(String ipAddress) {
    return ThrowableSupplier.of(() -> executeRequest(ipAddress));
  }

  private Optional<String> executeRequest(String ipAddress) {
    var localizationData = restTemplate.getForObject(buildUrl(ipAddress), LocalizationData.class);

    return Optional
      .ofNullable(localizationData)
      .map(it -> it.countryCode);
  }

  private String buildUrl(String ipAddress) {
    return String.format("http://ip-api.com/json/%s?fields=countryCode", ipAddress);
  }
}
