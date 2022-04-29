package site.neurotriumph.www.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.neurotriumph.www.annotation.AuthTokenPayload;
import site.neurotriumph.www.annotation.ConfirmationTokenPayload;
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
  public void confirmUserDeletion(@AuthTokenPayload DecodedJWT authTokenPayload,
                                  @Valid @RequestBody ConfirmationRequestBody confirmationRequestBody) {
    userService.confirmUserDeletion(authTokenPayload.getClaim(Field.USER_ID).asLong());
  }

  @DeleteMapping(baseUrl)
  @WithAuthToken
  public void deleteUser(@AuthTokenPayload DecodedJWT authTokenPayload,
                         @Valid @RequestBody DeleteUserRequestBody deleteUserRequestBody) {
    userService.deleteUser(authTokenPayload.getClaim(Field.USER_ID).asLong(),
      deleteUserRequestBody);
  }

  @PutMapping(baseUrl + "/email/confirm")
  @WithAuthToken
  @WithConfirmationToken(TokenMarker.EMAIL_UPDATE_CONFIRMATION)
  public void confirmEmailUpdate(@AuthTokenPayload DecodedJWT authTokenPayload,
                                 @Valid @RequestBody ConfirmationRequestBody confirmationRequestBody,
                                 @ConfirmationTokenPayload DecodedJWT confirmationTokenPayload) {
    userService.confirmEmailUpdate(authTokenPayload.getClaim(Field.USER_ID).asLong(),
      confirmationTokenPayload.getClaim(Field.NEW_EMAIL).asString());
  }

  @PutMapping(baseUrl + "/email")
  @WithAuthToken
  public void updateEmail(@AuthTokenPayload DecodedJWT authTokenPayload,
                          @Valid @RequestBody UpdateEmailRequestBody updateEmailRequestBody) {
    userService.updateEmail(authTokenPayload.getClaim(Field.USER_ID).asLong(),
      updateEmailRequestBody);
  }

  @PutMapping(baseUrl + "/password/confirm")
  @WithAuthToken
  @WithConfirmationToken(TokenMarker.PASSWORD_UPDATE_CONFIRMATION)
  public void confirmPasswordUpdate(@AuthTokenPayload DecodedJWT authTokenPayload,
                                    @Valid @RequestBody ConfirmationRequestBody confirmationRequestBody,
                                    @ConfirmationTokenPayload DecodedJWT confirmationTokenPayload) {
    userService.confirmPasswordUpdate(authTokenPayload.getClaim(Field.USER_ID).asLong(),
      confirmationTokenPayload.getClaim(Field.NEW_PASSWORD_HASH).asString());
  }

  @PutMapping(baseUrl + "/password")
  @WithAuthToken
  public void updatePassword(@AuthTokenPayload DecodedJWT authTokenPayload,
                             @Valid @RequestBody UpdatePasswordRequestBody updatePasswordRequestBody) {
    userService.updatePassword(authTokenPayload.getClaim(Field.USER_ID).asLong(), updatePasswordRequestBody);
  }

  @GetMapping(baseUrl)
  @WithAuthToken
  public GetUserResponseBody getUser(@AuthTokenPayload DecodedJWT authTokenPayload) {
    return userService.getUser(authTokenPayload.getClaim(Field.USER_ID).asLong());
  }
}
