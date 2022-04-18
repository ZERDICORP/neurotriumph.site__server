package site.neurotriumph.www.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.neurotriumph.www.pojo.RegisterRequestBody;
import site.neurotriumph.www.service.AuthService;

import javax.validation.Valid;

@RestController
@Validated
public class AuthController {
  @Autowired
 private AuthService authService;

  @PostMapping("/register")
  public void register(@Valid @RequestBody RegisterRequestBody registerRequestBody) {
    authService.register(registerRequestBody);
  }
}
