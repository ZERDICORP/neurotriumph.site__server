package site.neurotriumph.www.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.neurotriumph.www.annotation.WithConfirmationToken;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.ConfirmationRequestBody;
import site.neurotriumph.www.pojo.RegisterRequestBody;
import site.neurotriumph.www.service.AuthService;

import javax.validation.Valid;

@RestController
@Validated
public class AuthController {
  @Autowired
  private AuthService authService;

  @PutMapping("/register/confirm")
  @WithConfirmationToken(TokenMarker.REGISTRATION_CONFIRMATION)
  public void confirmRegistration(@Valid @RequestBody ConfirmationRequestBody confirmationRequestBody,
                                  DecodedJWT decodedJWT) {
    authService.confirmRegistration(decodedJWT);
  }

  @PostMapping("/register")
  public void register(@Valid @RequestBody RegisterRequestBody registerRequestBody) {
    authService.register(registerRequestBody);
  }
}
