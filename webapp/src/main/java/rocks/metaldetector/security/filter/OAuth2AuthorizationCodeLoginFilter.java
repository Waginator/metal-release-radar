package rocks.metaldetector.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rocks.metaldetector.persistence.domain.user.AbstractUserEntity;
import rocks.metaldetector.service.oauthAuthorizationState.OAuthAuthorizationStateService;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Component
@AllArgsConstructor
@Slf4j
public class OAuth2AuthorizationCodeLoginFilter extends OncePerRequestFilter {

  private final OAuthAuthorizationStateService authorizationStateService;
  private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().equals("/rest/v1/oauth/callback");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String state = request.getParameter("state");
    AbstractUserEntity user = authorizationStateService.findUserByState(state);
    if (user == null) {
      log.error("Cannot authenticate user returning from authorization code flow");
      response.sendError(SC_FORBIDDEN);
    }
    if (user != null) {
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
      authentication.setDetails(authenticationDetailsSource.buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      authorizationStateService.deleteByState(state);
    }

    filterChain.doFilter(request, response);
  }
}
