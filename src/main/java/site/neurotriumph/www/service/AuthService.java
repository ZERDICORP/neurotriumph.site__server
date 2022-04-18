package site.neurotriumph.www.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.RegisterRequestBody;
import site.neurotriumph.www.repository.UserRepository;

@Service
public class AuthService {
  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MailSenderService mailSenderService;

  public void register(RegisterRequestBody registerRequestBody) {
    userRepository.findByEmail(registerRequestBody.getEmail())
        .ifPresent(o -> { throw new IllegalStateException(Message.USER_ALREADY_EXISTS); });

    User user = userRepository.save(new User(
      registerRequestBody.getEmail(),
      DigestUtils.sha256Hex(registerRequestBody.getPassword())
    ));

    String token = JWT.create()
      .withClaim(Field.UID, user.getId())
      .sign(Algorithm.HMAC256(appSecret));

    mailSenderService.send(registerRequestBody.getEmail(), "Neuro Triumph", token);
  }
}
