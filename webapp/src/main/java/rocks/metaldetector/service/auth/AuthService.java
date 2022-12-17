package rocks.metaldetector.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rocks.metaldetector.persistence.domain.user.AbstractUserEntity;
import rocks.metaldetector.persistence.domain.user.UserRole;
import rocks.metaldetector.security.AuthenticationFacade;
import rocks.metaldetector.support.JwtsSupport;
import rocks.metaldetector.support.SecurityProperties;
import rocks.metaldetector.web.api.request.LoginRequest;
import rocks.metaldetector.web.api.auth.LoginResponse;

import java.time.Duration;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthService {

  private final JwtsSupport jwtsSupport;
  private final AuthenticationFacade authenticationFacade;
  private final AuthenticationManager authenticationManager;
  private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
  private HttpServletRequest httpRequest;
  private SecurityProperties securityProperties;

  @Transactional(readOnly = true)
  public LoginResponse loginUser(LoginRequest request) {
    authenticateUser(request);
    AbstractUserEntity user = authenticationFacade.getCurrentUser();
    String token = jwtsSupport.generateToken(
        user.getPublicId(),
        Duration.ofMinutes(securityProperties.getAccessTokenExpirationInMin())
    );
    return LoginResponse.builder()
        .username(request.getUsername())
        .accessToken(token)
        .roles(user.getUserRoles().stream().map(UserRole::getDisplayName).collect(Collectors.toList()))
        .build();
  }

  private void authenticateUser(LoginRequest request) {
    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
    authenticationToken.setDetails(authenticationDetailsSource.buildDetails(httpRequest));
    Authentication authentication = authenticationManager.authenticate(authenticationToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
