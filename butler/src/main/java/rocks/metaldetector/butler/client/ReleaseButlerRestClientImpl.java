package rocks.metaldetector.butler.client;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rocks.metaldetector.butler.api.ButlerImportJob;
import rocks.metaldetector.butler.api.ButlerImportResponse;
import rocks.metaldetector.butler.api.ButlerReleasesRequest;
import rocks.metaldetector.butler.api.ButlerReleasesResponse;
import rocks.metaldetector.butler.api.ButlerUpdateReleaseStateRequest;
import rocks.metaldetector.butler.config.ButlerConfig;
import rocks.metaldetector.support.exceptions.ExternalServiceException;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.PUT;

@Service
@Slf4j
@Profile({"default", "preview", "prod"})
@AllArgsConstructor
public class ReleaseButlerRestClientImpl implements ReleaseButlerRestClient {

  static final String UPDATE_ENDPOINT_PATH_PARAM = "/{releaseId}";

  private final RestTemplate releaseButlerRestTemplate;
  private final ButlerConfig butlerConfig;

  @Override
  public ButlerReleasesResponse queryAllReleases(ButlerReleasesRequest request) {
    HttpEntity<ButlerReleasesRequest> requestEntity = createQueryHttpEntity(request);

    ResponseEntity<ButlerReleasesResponse> responseEntity = releaseButlerRestTemplate.postForEntity(
        butlerConfig.getUnpaginatedReleasesUrl(),
        requestEntity,
        ButlerReleasesResponse.class
    );

    return handleReleaseResponseEntity(request, responseEntity);
  }

  @Override
  public ButlerReleasesResponse queryReleases(ButlerReleasesRequest request, String sort) {
    HttpEntity<ButlerReleasesRequest> requestEntity = createQueryHttpEntity(request);
    String sortingParameter = buildSortingParameter(sort);
    ResponseEntity<ButlerReleasesResponse> responseEntity = releaseButlerRestTemplate.postForEntity(
        butlerConfig.getReleasesUrl() + sortingParameter,
        requestEntity,
        ButlerReleasesResponse.class
    );

    return handleReleaseResponseEntity(request, responseEntity);
  }

  private ButlerReleasesResponse handleReleaseResponseEntity(ButlerReleasesRequest request, ResponseEntity<ButlerReleasesResponse> responseEntity) {
    ButlerReleasesResponse response = responseEntity.getBody();

    var shouldNotHappen = response == null || !responseEntity.getStatusCode().is2xxSuccessful();
    if (shouldNotHappen) {
      throw new ExternalServiceException("Could not get releases for request: '" + request + "' (Response code: " + responseEntity.getStatusCode() + ")");
    }

    return response;
  }

  @Override
  public void createImportJob() {
    ResponseEntity<Void> responseEntity = releaseButlerRestTemplate.postForEntity(butlerConfig.getImportUrl(), null, Void.class);
    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
      throw new ExternalServiceException("Could not create import job (Response code: " + responseEntity.getStatusCode() + ")");
    }
  }

  @Override
  public void createRetryCoverDownloadJob() {
    ResponseEntity<Void> responseEntity = releaseButlerRestTemplate.postForEntity(butlerConfig.getRetryCoverDownloadUrl(), null, Void.class);
    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
      throw new ExternalServiceException("Could not retry cover download (Response code: " + responseEntity.getStatusCode() + ")");
    }
  }

  @Override
  public List<ButlerImportJob> queryImportJobResults() {
    ResponseEntity<ButlerImportResponse> responseEntity = releaseButlerRestTemplate.getForEntity(
        butlerConfig.getImportUrl(),
        ButlerImportResponse.class
    );

    ButlerImportResponse response = responseEntity.getBody();
    var shouldNotHappen = response == null || !responseEntity.getStatusCode().is2xxSuccessful();
    if (shouldNotHappen) {
      throw new ExternalServiceException("Could not fetch import job results (Response code: " + responseEntity.getStatusCode() + ")");
    }

    return response.getImportJobs();
  }

  @Override
  public void updateReleaseState(long releaseId, String state) {
    ButlerUpdateReleaseStateRequest request = ButlerUpdateReleaseStateRequest.builder().state(state.toUpperCase()).build();
    HttpEntity<ButlerUpdateReleaseStateRequest> httpEntity = new HttpEntity<>(request);
    ResponseEntity<Void> responseEntity = releaseButlerRestTemplate.exchange(butlerConfig.getReleasesUrl() + UPDATE_ENDPOINT_PATH_PARAM,
                                                                             PUT,
                                                                             httpEntity,
                                                                             Void.class,
                                                                             Map.of("releaseId", releaseId));
    var shouldNotHappen = !responseEntity.getStatusCode().is2xxSuccessful();
    if (shouldNotHappen) {
      throw new ExternalServiceException("Could not update release state (Response code: " + responseEntity.getStatusCode() + ")");
    }
  }

  private HttpEntity<ButlerReleasesRequest> createQueryHttpEntity(ButlerReleasesRequest request) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setAcceptCharset(Collections.singletonList(Charset.defaultCharset()));
    return new HttpEntity<>(request, headers);
  }

  private String buildSortingParameter(String sort) {
    if (sort != null && !sort.isEmpty()) {
      return "?" + sort;
    }
    return "";
  }
}
