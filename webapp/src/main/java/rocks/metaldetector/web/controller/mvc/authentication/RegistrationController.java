package rocks.metaldetector.web.controller.mvc.authentication;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import rocks.metaldetector.service.exceptions.TokenExpiredException;
import rocks.metaldetector.service.token.TokenService;
import rocks.metaldetector.service.user.UserService;
import rocks.metaldetector.support.exceptions.ResourceNotFoundException;

import static rocks.metaldetector.support.Endpoints.Authentication.LOGIN;
import static rocks.metaldetector.support.Endpoints.Authentication.REGISTRATION_VERIFICATION;
import static rocks.metaldetector.support.Endpoints.Authentication.RESEND_VERIFICATION_TOKEN;

@Controller
@AllArgsConstructor
@Profile("!preview")
public class RegistrationController {

  private final UserService userService;
  private final TokenService tokenService;

  @GetMapping(REGISTRATION_VERIFICATION)
  public ModelAndView verifyRegistration(@RequestParam(value = "token") String tokenString) {
    String param = "verificationSuccess";

    try {
      userService.verifyEmailToken(tokenString);
    }
    catch (TokenExpiredException e) {
      param = "tokenExpired&token=" + tokenString;
    }
    catch (ResourceNotFoundException e) {
      param = "userNotFound";
    }

    return new ModelAndView("redirect:" + LOGIN + "?" + param);
  }

  @GetMapping(RESEND_VERIFICATION_TOKEN)
  public ModelAndView resendEmailVerificationToken(@RequestParam(value = "token") String tokenString) {
    String param = "resendVerificationTokenSuccess";

    try {
      tokenService.resendExpiredEmailVerificationToken(tokenString);
    }
    catch (ResourceNotFoundException e) {
      param = "userNotFound";
    }

    return new ModelAndView("redirect:" + LOGIN + "?" + param);
  }
}
