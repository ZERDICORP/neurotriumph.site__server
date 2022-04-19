package site.neurotriumph.www;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Regex;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.RegisterRequestBody;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.AuthService;
import site.neurotriumph.www.service.MailSenderService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RegisterUnitTest {
  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private AuthService authService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private MailSenderService mailSenderService;

  @Test(expected = IllegalStateException.class)
  public void shouldThrowIllegalStateExceptionBecauseUserAlreadyExists() {
    RegisterRequestBody registerRequestBody = new RegisterRequestBody(
      "valid@email.com",
      "Qwerty123");

    Mockito.doReturn(Optional.of(new User()))
      .when(userRepository)
      .findByEmail(registerRequestBody.getEmail());

    authService.register(registerRequestBody);
  }

  @Test
  public void shouldAddUserAndSendEmail() {
    RegisterRequestBody registerRequestBody = new RegisterRequestBody(
      "valid@email.com",
      "Qwerty123");

    User user = new User(
      registerRequestBody.getEmail(),
      registerRequestBody.getPassword());

    Mockito.doReturn(user)
      .when(userRepository)
      .save(ArgumentMatchers.any(User.class));

    JWTCreator.Builder builder = Mockito.spy(JWT.create());
    Mockito.mockStatic(JWT.class, Mockito.CALLS_REAL_METHODS);
    Mockito.when(JWT.create())
      .thenReturn(builder);

    final ResultCollector<String> resultCollector = new ResultCollector<>();
    Mockito.doAnswer(resultCollector)
      .when(builder)
      .sign(ArgumentMatchers.any(Algorithm.class));

    authService.register(registerRequestBody);

    Mockito.verify(userRepository, Mockito.times(1))
      .findByEmail(user.getEmail());

    Mockito.verify(userRepository, Mockito.times(1))
      .save(ArgumentMatchers.any(User.class));

    /*
     * Checking the token for validity.
     * */

    String token = resultCollector.getResult();

    assertDoesNotThrow(() -> {
      JWT.require(Algorithm.HMAC256(appSecret + TokenMarker.REGISTRATION_CONFIRMATION))
        .build()
        .verify(token);
    });

    DecodedJWT decodedJWT = JWT.decode(token);

    assertNotNull(decodedJWT.getClaim(Field.USER_ID));

    Mockito.verify(mailSenderService, Mockito.times(1))
      .send(
        ArgumentMatchers.eq(user.getEmail()),
        ArgumentMatchers.eq("Neuro Triumph"),
        ArgumentMatchers.matches(Regex.JWT_TOKEN));
  }
}
