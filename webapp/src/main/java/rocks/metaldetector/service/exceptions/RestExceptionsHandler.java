package rocks.metaldetector.service.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import rocks.metaldetector.support.exceptions.ExternalServiceException;
import rocks.metaldetector.support.exceptions.ResourceNotFoundException;
import rocks.metaldetector.web.api.response.ErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static rocks.metaldetector.service.auth.RefreshTokenService.REFRESH_TOKEN_COOKIE_NAME;
import static rocks.metaldetector.support.ErrorMessages.UNEXPECTED_EXTERNAL_SERVICE_BEHAVIOUR;
import static rocks.metaldetector.support.ErrorMessages.UNEXPECTED_SERVICE_BEHAVIOUR;

@ControllerAdvice
@Slf4j
public class RestExceptionsHandler {

  @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
  public ResponseEntity<ErrorResponse> handleBadRequests(Exception exception, WebRequest webRequest) {
    log.error("{}: {}", webRequest.getContextPath(), exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(BAD_REQUEST, exception), new HttpHeaders(), BAD_REQUEST);
  }

  @ExceptionHandler({MissingRequestCookieException.class})
  public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(MissingRequestCookieException exception, WebRequest webRequest) {
    if (!REFRESH_TOKEN_COOKIE_NAME.equals(exception.getCookieName())) {
      handleBadRequests(exception, webRequest);
    }

    return new ResponseEntity<>(createErrorResponse(UNAUTHORIZED, exception), new HttpHeaders(), UNAUTHORIZED);
  }

  @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
  public ResponseEntity<ErrorResponse> handleHttpMethodNotSupported(Exception exception, WebRequest webRequest) {
    log.warn("{}: {}", webRequest.getContextPath(), exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(METHOD_NOT_ALLOWED, exception), new HttpHeaders(), METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
  public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(Exception exception, WebRequest webRequest) {
    log.warn("{}: {}", webRequest.getContextPath(), exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(UNSUPPORTED_MEDIA_TYPE, exception), new HttpHeaders(), UNSUPPORTED_MEDIA_TYPE);
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ValidationException.class, MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
  public ResponseEntity<ErrorResponse> handleValidationErrors(Exception exception, WebRequest webRequest) {
    log.warn("{}: {}", webRequest.getContextPath(), exception.getMessage());
    BindingResult bindingResult = null;
    if (exception instanceof MethodArgumentNotValidException) {
      bindingResult = ((MethodArgumentNotValidException) exception).getBindingResult();
    }
    else if (exception instanceof BindException) {
      bindingResult = ((BindException) exception).getBindingResult();
    }

    return bindingResult != null
            ? new ResponseEntity<>(createErrorResponse(bindingResult), new HttpHeaders(), UNPROCESSABLE_ENTITY)
            : new ResponseEntity<>(createErrorResponse(UNPROCESSABLE_ENTITY, exception), new HttpHeaders(), UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler({UserAlreadyExistsException.class, IllegalUserActionException.class})
  public ResponseEntity<ErrorResponse> handleUserExceptions(RuntimeException exception, WebRequest webRequest) {
    log.warn("{}: {}", webRequest.getContextPath(), exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(CONFLICT, exception), new HttpHeaders(), CONFLICT);
  }

  @ExceptionHandler({ResourceNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFoundException(RuntimeException exception, WebRequest webRequest) {
    log.warn("{}: {}", webRequest.getContextPath(), exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(NOT_FOUND, exception), new HttpHeaders(), NOT_FOUND);
  }

  @ExceptionHandler({AccessDeniedException.class, OAuth2AuthorizationException.class})
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(RuntimeException exception, WebRequest webRequest) {
    log.warn("{}: {}", webRequest.getContextPath(), exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(FORBIDDEN, exception), new HttpHeaders(), FORBIDDEN);
  }

  @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class, AccountStatusException.class, ExpiredJwtException.class})
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(RuntimeException exception, WebRequest webRequest) {
    log.warn("{}: {}", webRequest.getContextPath(), exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(UNAUTHORIZED, exception), new HttpHeaders(), UNAUTHORIZED);
  }

  @ExceptionHandler({ExternalServiceException.class, RestClientException.class})
  public ResponseEntity<ErrorResponse> handleRestExceptions(RuntimeException exception, WebRequest webRequest) {
    log.error("{}: {}", webRequest.getContextPath(), exception.getMessage());
    return new ResponseEntity<>(
        createErrorResponse(SERVICE_UNAVAILABLE, UNEXPECTED_EXTERNAL_SERVICE_BEHAVIOUR.toDisplayString()),
        new HttpHeaders(),
        SERVICE_UNAVAILABLE
    );
  }

  @ExceptionHandler({Exception.class})
  public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception exception, WebRequest webRequest) {
    log.error("{}: {}", webRequest.getContextPath(), exception.getMessage(), exception);
    return new ResponseEntity<>(
        createErrorResponse(INTERNAL_SERVER_ERROR, UNEXPECTED_SERVICE_BEHAVIOUR.toDisplayString()),
        new HttpHeaders(),
        INTERNAL_SERVER_ERROR);
  }

  private ErrorResponse createErrorResponse(HttpStatus status, Throwable exception) {
    return new ErrorResponse(status.value(), status.getReasonPhrase(), List.of(exception.getMessage()));
  }

  private ErrorResponse createErrorResponse(HttpStatus status, String message) {
    return new ErrorResponse(status.value(), status.getReasonPhrase(), List.of(message));
  }

  private ErrorResponse createErrorResponse(BindingResult bindingResult) {
    List<String> fieldErrors = bindingResult.getAllErrors()
            .stream()
            .map(this::transformFieldError)
            .sorted()
            .collect(Collectors.toList());

    return new ErrorResponse(UNPROCESSABLE_ENTITY.value(), UNPROCESSABLE_ENTITY.getReasonPhrase(), fieldErrors);
  }

  private String transformFieldError(ObjectError error) {
    if (error instanceof FieldError) {
      return "Error regarding field '" +
              ((FieldError) error).getField() +
              "': " +
              error.getDefaultMessage() +
              ".";
    }
    else {
      return error.getDefaultMessage();
    }
  }
}
