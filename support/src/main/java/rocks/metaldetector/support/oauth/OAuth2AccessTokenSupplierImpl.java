package rocks.metaldetector.support.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

@Component
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class OAuth2AccessTokenSupplierImpl implements OAuth2AccessTokenSupplier {

  static final AnonymousAuthenticationToken PRINCIPAL = new AnonymousAuthenticationToken("key", "anonymous", createAuthorityList("ROLE_ANONYMOUS"));

  private final OAuth2AuthorizedClientManager manager;
  private String registrationId;

  @Override
  public String get() {
    OAuth2AuthorizeRequest authorizedRequest = OAuth2AuthorizeRequest
        .withClientRegistrationId(registrationId)
        .principal(PRINCIPAL)
        .build();
    OAuth2AuthorizedClient authorizedClient = manager.authorize(authorizedRequest);
    if (authorizedClient == null || authorizedClient.getAccessToken() == null ||
        authorizedClient.getAccessToken().getTokenValue() == null || authorizedClient.getAccessToken().getTokenValue().isEmpty()) {
      throw new IllegalArgumentException("No access token for client '" + registrationId + "'");
    }
    return authorizedClient.getAccessToken().getTokenValue();
  }

  @Override
  public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
  }
}
