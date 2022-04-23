package site.neurotriumph.www.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.neurotriumph.www.annotation.WithAuthToken;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.pojo.GetUserResponseBody;
import site.neurotriumph.www.pojo.UpdatePasswordRequestBody;
import site.neurotriumph.www.service.UserService;

import javax.validation.Valid;

@RestController
@Validated
public class UserController {
  @Autowired
  private UserService userService;

  @PutMapping("/user/password")
  @WithAuthToken
  public void updatePassword(@Valid @RequestBody UpdatePasswordRequestBody updatePasswordRequestBody,
                             DecodedJWT decodedJWT) {
    userService.updatePassword(updatePasswordRequestBody,
      decodedJWT.getClaim(Field.USER_ID).asLong());
  }

  @GetMapping("/user")
  @WithAuthToken
  public GetUserResponseBody getUser(DecodedJWT decodedJWT) {
    return userService.getUser(decodedJWT.getClaim(Field.USER_ID).asLong());
  }
}
