package rocks.metaldetector.service;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import rocks.metaldetector.persistence.domain.user.UserRole;
import rocks.metaldetector.security.AuthenticationFacade;
import rocks.metaldetector.support.JwtsSupport;
import rocks.metaldetector.web.api.request.LoginRequest;
import rocks.metaldetector.web.api.response.LoginResponse;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LoginService {

  private final JwtsSupport jwtsSupport;
  private final AuthenticationFacade authenticationFacade;
  private final AuthenticationManager authenticationManager;
  private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
  private HttpServletRequest httpRequest;

  public LoginResponse loginUser(LoginRequest request) {
    authenticateUser(request);
    var user = authenticationFacade.getCurrentUser();
    var token = jwtsSupport.generateToken(user.getPublicId(), Duration.ofHours(1));
    return LoginResponse.builder()
        .username(request.getUsername())
        .token(token)
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
