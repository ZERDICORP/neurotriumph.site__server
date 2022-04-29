package site.neurotriumph.www.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.neurotriumph.www.annotation.ConfirmationTokenPayload;
import site.neurotriumph.www.annotation.WithConfirmationToken;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.ConfirmationRequestBody;
import site.neurotriumph.www.pojo.LoginRequestBody;
import site.neurotriumph.www.pojo.LoginResponseBody;
import site.neurotriumph.www.pojo.RegisterRequestBody;
import site.neurotriumph.www.service.AuthService;

import javax.validation.Valid;

@RestController
@Validated
public class AuthController {
  @Autowired
  private AuthService authService;

  @PostMapping("/login")
  public LoginResponseBody login(@Valid @RequestBody LoginRequestBody loginRequestBody) {
    return authService.login(loginRequestBody);
  }

  @PutMapping("/register/confirm")
  @WithConfirmationToken(TokenMarker.REGISTRATION_CONFIRMATION)
  public void confirmRegistration(@Valid @RequestBody ConfirmationRequestBody confirmationRequestBody,
                                  @ConfirmationTokenPayload DecodedJWT confirmationTokenPayload) {
    authService.confirmRegistration(confirmationTokenPayload.getClaim(Field.USER_ID).asLong());
  }

  @PostMapping("/register")
  public void register(@Valid @RequestBody RegisterRequestBody registerRequestBody) {
    authService.register(registerRequestBody);
  }
}
