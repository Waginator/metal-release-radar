package rocks.metaldetector.butler.client;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rocks.metaldetector.butler.api.ButlerImportResponse;
import rocks.metaldetector.butler.api.ButlerReleasesRequest;
import rocks.metaldetector.butler.api.ButlerReleasesResponse;
import rocks.metaldetector.support.ExternalServiceException;

import java.nio.charset.Charset;
import java.util.Collections;

@Service
@Slf4j
@Profile({"default", "preview", "prod"})
@AllArgsConstructor
public class ReleaseButlerRestClientImpl implements ReleaseButlerRestClient {

  private final RestTemplate restTemplate;
  private final String releasesEndpoint;
  private final String importEndpoint;

  @Override
  public ButlerReleasesResponse queryReleases(ButlerReleasesRequest request) {
    HttpEntity<ButlerReleasesRequest> requestEntity = createQueryHttpEntity(request);

    ResponseEntity<ButlerReleasesResponse> responseEntity = restTemplate.postForEntity(releasesEndpoint, requestEntity, ButlerReleasesResponse.class);
    ButlerReleasesResponse response = responseEntity.getBody();

    var shouldNotHappen = response == null || !responseEntity.getStatusCode().is2xxSuccessful();
    if (shouldNotHappen) {
      throw new ExternalServiceException("Could not get releases for request: '" + request + "' (Response code: " + responseEntity.getStatusCode() + ")");
    }

    return response;
  }

  @Override
  public ButlerImportResponse importReleases() {
    HttpEntity<Object> requestEntity = createImportHttpEntity();

    ResponseEntity<ButlerImportResponse> responseEntity = restTemplate.exchange(importEndpoint, HttpMethod.GET, requestEntity, ButlerImportResponse.class);
    ButlerImportResponse response = responseEntity.getBody();

    var shouldNotHappen = response == null || !responseEntity.getStatusCode().is2xxSuccessful();
    if (shouldNotHappen) {
      throw new ExternalServiceException("Could not import releases (Response code : " + responseEntity.getStatusCode() + ")");
    }

    return response;
  }

  private HttpEntity<ButlerReleasesRequest> createQueryHttpEntity(ButlerReleasesRequest request) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setAcceptCharset(Collections.singletonList(Charset.defaultCharset()));
    return new HttpEntity<>(request, headers);
  }

  private HttpEntity<Object> createImportHttpEntity() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setAcceptCharset(Collections.singletonList(Charset.defaultCharset()));
    return new HttpEntity<>(headers);
  }
}
