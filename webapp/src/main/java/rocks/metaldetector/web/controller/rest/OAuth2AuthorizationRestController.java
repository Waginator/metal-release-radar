package rocks.metaldetector.web.controller.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rocks.metaldetector.security.CurrentUserSupplier;
import rocks.metaldetector.support.Endpoints;
import rocks.metaldetector.web.api.response.OAuth2UserAuthorizationExistsResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(Endpoints.Rest.OAUTH)
@AllArgsConstructor
public class OAuth2AuthorizationRestController {

  private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
  private final CurrentUserSupplier currentUserSupplier;

  @GetMapping(path = "/{registration-id}", produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<OAuth2UserAuthorizationExistsResponse> checkAuthorization(@PathVariable("registration-id") String registrationId) {
    OAuth2AuthorizedClient authorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(registrationId, currentUserSupplier.get().getUsername());
    if (authorizedClient != null) {
      return ResponseEntity.ok(new OAuth2UserAuthorizationExistsResponse(true));
    }
    return ResponseEntity.ok(new OAuth2UserAuthorizationExistsResponse(false));
  }

  @DeleteMapping(path = "/{registration-id}")
  public ResponseEntity<Void> deleteAuthorization(@PathVariable("registration-id") String registrationId) {
    oAuth2AuthorizedClientService.removeAuthorizedClient(registrationId, currentUserSupplier.get().getUsername());
    return ResponseEntity.ok().build();
  }
}
