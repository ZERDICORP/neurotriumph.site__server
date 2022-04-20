package site.neurotriumph.www.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.neurotriumph.www.constant.Const;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.LoginRequestBody;
import site.neurotriumph.www.pojo.LoginResponseBody;
import site.neurotriumph.www.pojo.RegisterRequestBody;
import site.neurotriumph.www.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.Date;

@Service
public class AuthService {
  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MailSenderService mailSenderService;

  public LoginResponseBody login(LoginRequestBody loginRequestBody) {
    User user = userRepository.findByEmail(loginRequestBody.getEmail())
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    if (!user.isConfirmed())
      throw new IllegalStateException(Message.USER_NOT_CONFIRMED);

    if (!user.getPassword_hash()
      .equals(DigestUtils.sha256Hex(loginRequestBody.getPassword())))
      throw new IllegalStateException(Message.WRONG_PASSWORD);

    String token = JWT.create()
      .withClaim(Field.USER_ID, user.getId())
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.AUTH_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    return new LoginResponseBody(token, user.getId());
  }

  @Transactional
  public void confirmRegistration(DecodedJWT decodedJWT) {
    User user = userRepository.findById(decodedJWT.getClaim(Field.USER_ID).asLong())
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    if (user.isConfirmed())
      throw new IllegalStateException(Message.USER_ALREADY_CONFIRMED);

    user.setConfirmed(true);
  }

  public void register(RegisterRequestBody registerRequestBody) {
    userRepository.findByEmail(registerRequestBody.getEmail())
      .ifPresent(o -> {
        throw new IllegalStateException(Message.USER_ALREADY_EXISTS);
      });

    User user = userRepository.save(new User(
      registerRequestBody.getEmail(),
      DigestUtils.sha256Hex(registerRequestBody.getPassword())
    ));

    String token = JWT.create()
      .withClaim(Field.USER_ID, user.getId())
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.CONFIRMATION_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.REGISTRATION_CONFIRMATION));

    mailSenderService.send(registerRequestBody.getEmail(), "Neuro Triumph", token);
  }
}
