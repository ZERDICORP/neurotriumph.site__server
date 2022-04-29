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
import site.neurotriumph.www.pojo.DeleteUserRequestBody;
import site.neurotriumph.www.pojo.GetUserResponseBody;
import site.neurotriumph.www.pojo.UpdateEmailRequestBody;
import site.neurotriumph.www.pojo.UpdatePasswordRequestBody;
import site.neurotriumph.www.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.Date;

@Service
public class UserService {
  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MailSenderService mailSenderService;

  public void confirmUserDeletion(Long id) {
    userRepository.findConfirmedById(id)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    userRepository.deleteById(id);
  }

  public void deleteUser(Long id, DeleteUserRequestBody deleteUserRequestBody) {
    User user = userRepository.findConfirmedById(id)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    if (!user.getPassword_hash()
      .equals(DigestUtils.sha256Hex(deleteUserRequestBody.getPassword()))) {
      throw new IllegalStateException(Message.WRONG_PASSWORD);
    }

    String token = JWT.create()
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.CONFIRMATION_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.USER_DELETE_CONFIRMATION));

    mailSenderService.send(user.getEmail(), "Neuro Triumph", token);
  }

  @Transactional
  public void confirmEmailUpdate(Long id, String newEmail) {
    User user = userRepository.findConfirmedById(id)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    if (user.getEmail().equals(newEmail)) {
      throw new IllegalStateException(Message.NOTHING_TO_UPDATE);
    }

    user.setEmail(newEmail);
  }

  public void updateEmail(Long id, UpdateEmailRequestBody updateEmailRequestBody) {
    User user = userRepository.findConfirmedById(id)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    if (!user.getPassword_hash()
      .equals(DigestUtils.sha256Hex(updateEmailRequestBody.getPassword()))) {
      throw new IllegalStateException(Message.WRONG_PASSWORD);
    }

    if (user.getEmail().equals(updateEmailRequestBody.getNew_email())) {
      throw new IllegalStateException(Message.NOTHING_TO_UPDATE);
    }

    String token = JWT.create()
      .withClaim(Field.NEW_EMAIL, updateEmailRequestBody.getNew_email())
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.CONFIRMATION_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.EMAIL_UPDATE_CONFIRMATION));

    mailSenderService.send(user.getEmail(), "Neuro Triumph", token);
  }

  @Transactional
  public void confirmPasswordUpdate(Long id, String newPasswordHash) {
    User user = userRepository.findConfirmedById(id)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    if (user.getPassword_hash().equals(newPasswordHash)) {
      throw new IllegalStateException(Message.NOTHING_TO_UPDATE);
    }

    user.setPassword_hash(newPasswordHash);
  }

  public void updatePassword(Long id, UpdatePasswordRequestBody updatePasswordRequestBody) {
    User user = userRepository.findConfirmedById(id)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    if (!user.getPassword_hash()
      .equals(DigestUtils.sha256Hex(updatePasswordRequestBody.getPassword()))) {
      throw new IllegalStateException(Message.WRONG_PASSWORD);
    }

    String newPasswordHash = DigestUtils.sha256Hex(updatePasswordRequestBody.getNew_password());

    if (user.getPassword_hash().equals(newPasswordHash)) {
      throw new IllegalStateException(Message.NOTHING_TO_UPDATE);
    }

    String token = JWT.create()
      .withClaim(Field.NEW_PASSWORD_HASH, newPasswordHash)
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
