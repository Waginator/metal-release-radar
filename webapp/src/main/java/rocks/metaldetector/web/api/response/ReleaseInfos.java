package rocks.metaldetector.web.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseInfos {

  Map<YearMonth, Integer> releasesPerMonth;
  int totalReleases;
  int upcomingReleases;
  int releasesThisMonth;
  int duplicates;
}
