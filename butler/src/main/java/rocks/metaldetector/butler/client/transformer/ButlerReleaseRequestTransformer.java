package rocks.metaldetector.butler.client.transformer;

import org.springframework.stereotype.Service;
import rocks.metaldetector.butler.api.ButlerReleasesRequest;
import rocks.metaldetector.support.PageRequest;
import rocks.metaldetector.support.TimeRange;

@Service
public class ButlerReleaseRequestTransformer {

  public ButlerReleasesRequest transform(Iterable<String> artists, TimeRange timeRange, PageRequest pageRequest) {
    return ButlerReleasesRequest.builder()
            .page(pageRequest != null ? pageRequest.getPage() : 0)
            .size(pageRequest != null ? pageRequest.getSize() : 0)
            .artists(artists)
            .dateFrom(timeRange.getDateFrom())
            .dateTo(timeRange.getDateTo())
            .sorting(pageRequest != null ? pageRequest.getSort() != null ? pageRequest.getSort().toString() : "" : "")
            .build();
  }
}
