package site.neurotriumph.www.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.neurotriumph.www.constant.Const;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.GetUserResponseBody;
import site.neurotriumph.www.pojo.UpdatePasswordRequestBody;
import site.neurotriumph.www.repository.UserRepository;

import java.util.Date;

@Service
public class UserService {
  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MailSenderService mailSenderService;

  public void updatePassword(UpdatePasswordRequestBody updatePasswordRequestBody, Long id) {
    User user = userRepository.findConfirmedById(id)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    if (!user.getPassword_hash()
      .equals(DigestUtils.sha256Hex(updatePasswordRequestBody.getPassword()))) {
      throw new IllegalStateException(Message.WRONG_PASSWORD);
    }

    if (user.getPassword_hash()
      .equals(DigestUtils.sha256Hex(updatePasswordRequestBody.getNew_password()))) {
      throw new IllegalStateException(Message.DATA_IS_NOT_CHANGED);
    }

    String token = JWT.create()
      .withClaim(Field.USER_ID, user.getId())
      .withClaim(Field.NEW_PASSWORD, updatePasswordRequestBody.getNew_password())
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.CONFIRMATION_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.PASSWORD_UPDATE_CONFIRMATION));

    mailSenderService.send(user.getEmail(), "Neuro Triumph", token);
  }

  public GetUserResponseBody getUser(Long id) {
    User user = userRepository.findConfirmedById(id)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    return new GetUserResponseBody(user.getEmail());
  }
}
