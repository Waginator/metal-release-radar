package rocks.metaldetector.config.logging;

import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

import static rocks.metaldetector.support.Endpoints.Rest.CURRENT_USER_PASSWORD;

public class RestRequestLoggingFilter extends CommonsRequestLoggingFilter {

  RestRequestLoggingFilter() {
    super.setIncludeQueryString(true);
    super.setIncludePayload(true);
    super.setIncludeClientInfo(true);
    super.setMaxPayloadLength(10000);
    super.setIncludeHeaders(false);
    super.setAfterMessagePrefix("");
  }

  @Override
  protected boolean shouldLog(HttpServletRequest request) {
    var requestUri = request.getRequestURI().toLowerCase();
    return requestUri.startsWith("/rest") && !requestUri.startsWith(CURRENT_USER_PASSWORD);
  }

  @Override
  protected void beforeRequest(HttpServletRequest request, String message) {
  }

  @Override
  protected void afterRequest(HttpServletRequest request, String message) {
    logger.info(message);
  }
}
