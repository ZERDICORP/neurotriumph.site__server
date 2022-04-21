package site.neurotriumph.www.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import site.neurotriumph.www.annotation.WithAuthToken;
import site.neurotriumph.www.pojo.GetUserResponseBody;
import site.neurotriumph.www.service.UserService;

@RestController
@Validated
public class UserController {
  @Autowired
  private UserService userService;

  @GetMapping("/user")
  @WithAuthToken
  public GetUserResponseBody getUser(DecodedJWT decodedJWT) {
    return userService.getUser(decodedJWT);
  }
}
