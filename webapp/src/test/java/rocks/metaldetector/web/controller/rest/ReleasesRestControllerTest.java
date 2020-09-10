package rocks.metaldetector.web.controller.rest;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rocks.metaldetector.butler.facade.ReleaseService;
import rocks.metaldetector.butler.facade.dto.ImportJobResultDto;
import rocks.metaldetector.config.constants.Endpoints;
import rocks.metaldetector.service.exceptions.RestExceptionsHandler;
import rocks.metaldetector.support.PageRequest;
import rocks.metaldetector.support.TimeRange;
import rocks.metaldetector.testutil.DtoFactory.ImportJobResultDtoFactory;
import rocks.metaldetector.testutil.DtoFactory.ReleaseDtoFactory;
import rocks.metaldetector.testutil.DtoFactory.ReleaseRequestFactory;
import rocks.metaldetector.web.RestAssuredMockMvcUtils;
import rocks.metaldetector.web.api.request.PaginatedReleasesRequest;
import rocks.metaldetector.web.api.request.ReleasesRequest;
import rocks.metaldetector.web.api.response.ReleasesResponse;
import rocks.metaldetector.web.transformer.ReleasesResponseTransformer;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static rocks.metaldetector.testutil.DtoFactory.PaginatedReleaseRequestFactory;

@ExtendWith(MockitoExtension.class)
class ReleasesRestControllerTest implements WithAssertions {

  @Mock
  private ReleaseService releasesService;

  @Mock
  private ReleasesResponseTransformer releasesResponseTransformer;

  @InjectMocks
  private ReleasesRestController underTest;

  @BeforeEach
  void setUp() {
    RestAssuredMockMvc.standaloneSetup(underTest,
                                       springSecurity((request, response, chain) -> chain.doFilter(request, response)),
                                       RestExceptionsHandler.class);
  }

  @AfterEach
  void tearDown() {
    reset(releasesService, releasesResponseTransformer);
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @DisplayName("Tests for endpoint '" + Endpoints.Rest.QUERY_ALL_RELEASES + "'")
  class QueryAllReleasesTest {

    private RestAssuredMockMvcUtils restAssuredUtils;

    @BeforeEach
    void setUp() {
      restAssuredUtils = new RestAssuredMockMvcUtils(Endpoints.Rest.QUERY_ALL_RELEASES);
    }

    @Test
    @DisplayName("Should pass query request parameter to release service")
    void should_pass_query_parameter_to_release_service() {
      // given
      ReleasesRequest request = ReleaseRequestFactory.createDefault();

      // when
      restAssuredUtils.doGet(toMap(request));

      // then
      verify(releasesService, times(1)).findAllReleases(Collections.emptyList(), new TimeRange(request.getDateFrom(), request.getDateTo()));
    }

    @Test
    @DisplayName("Should use transformer to transform each ReleaseDto")
    void should_use_releases_transformer() {
      // given
      var request = ReleaseRequestFactory.createDefault();
      var release1 = ReleaseDtoFactory.withArtistName("Metallica");
      var release2 = ReleaseDtoFactory.withArtistName("Slayer");
      doReturn(List.of(release1, release2)).when(releasesService).findAllReleases(any(), any());

      // when
      restAssuredUtils.doGet(toMap(request));

      // then
      verify(releasesResponseTransformer, times(1)).transform(eq(release1));
      verify(releasesResponseTransformer, times(1)).transform(eq(release2));
    }

    @Test
    @DisplayName("Should return the transformed releases response")
    void should_return_releases() {
      // given
      var request = ReleaseRequestFactory.createDefault();
      var transformedResponse = new ReleasesResponse();
      doReturn(List.of(ReleaseDtoFactory.createDefault())).when(releasesService).findAllReleases(any(), any());
      doReturn(transformedResponse).when(releasesResponseTransformer).transform(any());

      // when
      var validatableResponse = restAssuredUtils.doGet(toMap(request));

      // then
      validatableResponse
          .contentType(ContentType.JSON)
          .statusCode(OK.value());

      var result = validatableResponse.extract().as(ReleasesResponse[].class);
      assertThat(Arrays.asList(result)).isEqualTo(List.of(transformedResponse));
    }

    @ParameterizedTest(name = "Should return 400 on invalid query request <{0}>")
    @MethodSource("requestProvider")
    @DisplayName("Should return 400 on invalid query request")
    void test_invalid_query_requests(ReleasesRequest request) {
      // when
      var validatableResponse = restAssuredUtils.doGet(toMap(request));

      // then
      validatableResponse
          .contentType(ContentType.JSON)
          .statusCode(BAD_REQUEST.value());
    }

    private Stream<Arguments> requestProvider() {
      var validFrom = LocalDate.now();
      var validTo = LocalDate.now().plusDays(10);

      return Stream.of(
              Arguments.of(new ReleasesRequest(validFrom.plusDays(20), validTo))
      );
    }

    private Map<String, Object> toMap(ReleasesRequest request) {
      Map<String, Object> map = new HashMap<>();
      map.put("dateFrom", request.getDateFrom().toString());
      map.put("dateTo", request.getDateTo().toString());

      return map;
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @DisplayName("Tests for endpoint '" + Endpoints.Rest.QUERY_RELEASES + "'")
  class QueryReleasesTest {

    private RestAssuredMockMvcUtils restAssuredUtils;

    @BeforeEach
    void setUp() {
      restAssuredUtils = new RestAssuredMockMvcUtils(Endpoints.Rest.QUERY_RELEASES);
    }

    @Test
    @DisplayName("Should pass query request parameter to release service")
    void should_pass_query_parameter_to_release_service() {
      // given
      PaginatedReleasesRequest request = PaginatedReleaseRequestFactory.createDefault();

      // when
      restAssuredUtils.doGet(toMap(request));

      // then
      verify(releasesService, times(1)).findReleases(
              Collections.emptyList(),
              new TimeRange(request.getDateFrom(), request.getDateTo()),
              new PageRequest(request.getPage(), request.getSize())
      );
    }

    @Test
    @DisplayName("Should use transformer to transform each ReleaseDto")
    void should_use_releases_transformer() {
      // given
      var request = PaginatedReleaseRequestFactory.createDefault();
      var release1 = ReleaseDtoFactory.withArtistName("Metallica");
      var release2 = ReleaseDtoFactory.withArtistName("Slayer");
      doReturn(List.of(release1, release2)).when(releasesService).findReleases(any(), any(), any());

      // when
      restAssuredUtils.doGet(toMap(request));

      // then
      verify(releasesResponseTransformer, times(1)).transform(eq(release1));
      verify(releasesResponseTransformer, times(1)).transform(eq(release2));
    }

    @Test
    @DisplayName("Should return the transformed releases response")
    void should_return_releases() {
      // given
      var request = PaginatedReleaseRequestFactory.createDefault();
      var transformedResponse = new ReleasesResponse();
      doReturn(List.of(ReleaseDtoFactory.createDefault())).when(releasesService).findReleases(any(), any(), any());
      doReturn(transformedResponse).when(releasesResponseTransformer).transform(any());

      // when
      var validatableResponse = restAssuredUtils.doGet(toMap(request));

      // then
      validatableResponse
              .contentType(ContentType.JSON)
              .statusCode(OK.value());

      var result = validatableResponse.extract().as(ReleasesResponse[].class);
      assertThat(Arrays.asList(result)).isEqualTo(List.of(transformedResponse));
    }

    @ParameterizedTest(name = "Should return 400 on invalid query request <{0}>")
    @MethodSource("requestProvider")
    @DisplayName("Should return 400 on invalid query request")
    void test_invalid_query_requests(PaginatedReleasesRequest request) {
      // when
      var validatableResponse = restAssuredUtils.doGet(toMap(request));

      // then
      validatableResponse
              .contentType(ContentType.JSON)
              .statusCode(BAD_REQUEST.value());
    }

    private Stream<Arguments> requestProvider() {
      var validPage = 1;
      var validSize = 10;
      var validFrom = LocalDate.now();
      var validTo = LocalDate.now().plusDays(10);

      return Stream.of(
              Arguments.of(new PaginatedReleasesRequest(0, validSize, validFrom, validTo)),
              Arguments.of(new PaginatedReleasesRequest(validPage, 0, validFrom, validTo)),
              Arguments.of(new PaginatedReleasesRequest(validPage, 51, validFrom, validTo)),
              Arguments.of(new PaginatedReleasesRequest(validPage, validSize, validFrom.plusDays(20), validTo))
      );
    }

    private Map<String, Object> toMap(PaginatedReleasesRequest request) {
      Map<String, Object> map = new HashMap<>();
      map.put("page", request.getPage());
      map.put("size", request.getSize());
      map.put("dateFrom", request.getDateFrom().toString());
      map.put("dateTo", request.getDateTo().toString());

      return map;
    }
  }

  @Nested
  @DisplayName("Tests creating an import job")
  class CreateImportTest {

    private RestAssuredMockMvcUtils restAssuredUtils;

    @BeforeEach
    void setUp() {
      restAssuredUtils = new RestAssuredMockMvcUtils(Endpoints.Rest.IMPORT_JOB);
    }

    @Test
    @DisplayName("Should call release service")
    void should_call_release_service() {
      // when
      restAssuredUtils.doPost();

      // then
      verify(releasesService, times(1)).createImportJob();
    }

    @Test
    @DisplayName("Should return CREATED")
    void should_return_status_created() {
      // when
      ValidatableMockMvcResponse validatableResponse = restAssuredUtils.doPost();

      // then
      validatableResponse.statusCode(CREATED.value());
    }
  }

  @Nested
  @DisplayName("Tests creating a job for retrying cover downloads")
  class CreateRetryDownloadTest {

    private RestAssuredMockMvcUtils restAssuredUtils;

    @BeforeEach
    void setUp() {
      restAssuredUtils = new RestAssuredMockMvcUtils(Endpoints.Rest.COVER_JOB);
    }

    @Test
    @DisplayName("Should call release service")
    void should_call_release_service() {
      // when
      restAssuredUtils.doPost();

      // then
      verify(releasesService, times(1)).createRetryCoverDownloadJob();
    }

    @Test
    @DisplayName("Should return OK")
    void should_return_status_ok() {
      // when
      ValidatableMockMvcResponse validatableResponse = restAssuredUtils.doPost();

      // then
      validatableResponse.statusCode(OK.value());
    }
  }

  @Nested
  @DisplayName("Tests for querying import job results")
  class QueryImportJobResultsTest {

    private RestAssuredMockMvcUtils restAssuredUtils;

    @BeforeEach
    void setUp() {
      restAssuredUtils = new RestAssuredMockMvcUtils(Endpoints.Rest.IMPORT_JOB);
    }

    @Test
    @DisplayName("Should call release service")
    void should_call_release_service() {
      // when
      restAssuredUtils.doGet();

      // then
      verify(releasesService, times(1)).queryImportJobResults();
    }

    @Test
    @DisplayName("Should return result from release service with status OK")
    void should_return_status_created() {
      // given
      var importJobResultDto = List.of(
              ImportJobResultDtoFactory.createDefault(),
              ImportJobResultDtoFactory.createDefault()
      );
      doReturn(importJobResultDto).when(releasesService).queryImportJobResults();

      // when
      ValidatableMockMvcResponse validatableResponse = restAssuredUtils.doGet();

      // then
      validatableResponse.statusCode(OK.value());
      var result = validatableResponse.extract().body().jsonPath().getList(".", ImportJobResultDto.class);
      assertThat(result).isEqualTo(importJobResultDto);
    }
  }
}
