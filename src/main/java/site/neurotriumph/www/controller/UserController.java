package site.neurotriumph.www.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.neurotriumph.www.annotation.WithAuthToken;
import site.neurotriumph.www.annotation.WithConfirmationToken;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.ConfirmationRequestBody;
import site.neurotriumph.www.pojo.DeleteUserRequestBody;
import site.neurotriumph.www.pojo.GetUserResponseBody;
import site.neurotriumph.www.pojo.UpdateEmailRequestBody;
import site.neurotriumph.www.pojo.UpdatePasswordRequestBody;
import site.neurotriumph.www.service.UserService;

import javax.validation.Valid;

@RestController
@Validated
public class UserController {
  private final String baseUrl = "/user";

  @Autowired
  private UserService userService;

  @PutMapping(baseUrl + "/delete/confirm")
  @WithAuthToken
  @WithConfirmationToken(TokenMarker.USER_DELETE_CONFIRMATION)
  public void confirmUserDeletion(@Valid @RequestBody ConfirmationRequestBody confirmationRequestBody,
                                  DecodedJWT decodedJWT) {
    userService.confirmUserDeletion(decodedJWT.getClaim(Field.USER_ID).asLong());
  }

  @DeleteMapping(baseUrl)
  @WithAuthToken
  public void deleteUser(@Valid @RequestBody DeleteUserRequestBody deleteUserRequestBody,
                         DecodedJWT decodedJWT) {
    userService.deleteUser(deleteUserRequestBody,
      decodedJWT.getClaim(Field.USER_ID).asLong());
  }

  @PutMapping(baseUrl + "/email/confirm")
  @WithAuthToken
  @WithConfirmationToken(TokenMarker.EMAIL_UPDATE_CONFIRMATION)
  public void confirmEmailUpdate(@Valid @RequestBody ConfirmationRequestBody confirmationRequestBody,
                                 DecodedJWT decodedJWT) {
    userService.confirmEmailUpdate(decodedJWT.getClaim(Field.USER_ID).asLong(),
      decodedJWT.getClaim(Field.NEW_EMAIL).asString());
  }

  @PutMapping(baseUrl + "/email")
  @WithAuthToken
  public void updateEmail(@Valid @RequestBody UpdateEmailRequestBody updateEmailRequestBody,
                          DecodedJWT decodedJWT) {
    userService.updateEmail(updateEmailRequestBody,
      decodedJWT.getClaim(Field.USER_ID).asLong());
  }

  @PutMapping(baseUrl + "/password/confirm")
  @WithAuthToken
  @WithConfirmationToken(TokenMarker.PASSWORD_UPDATE_CONFIRMATION)
  public void confirmPasswordUpdate(@Valid @RequestBody ConfirmationRequestBody confirmationRequestBody,
                                    DecodedJWT decodedJWT) {
    userService.confirmPasswordUpdate(decodedJWT.getClaim(Field.USER_ID).asLong(),
      decodedJWT.getClaim(Field.NEW_PASSWORD_HASH).asString());
  }

  @PutMapping(baseUrl + "/password")
  @WithAuthToken
  public void updatePassword(@Valid @RequestBody UpdatePasswordRequestBody updatePasswordRequestBody,
                             DecodedJWT decodedJWT) {
    userService.updatePassword(updatePasswordRequestBody,
      decodedJWT.getClaim(Field.USER_ID).asLong());
  }

  @GetMapping(baseUrl)
  @WithAuthToken
  public GetUserResponseBody getUser(DecodedJWT decodedJWT) {
    return userService.getUser(decodedJWT.getClaim(Field.USER_ID).asLong());
  }
}
