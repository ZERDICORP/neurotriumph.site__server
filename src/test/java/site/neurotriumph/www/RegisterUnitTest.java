package site.neurotriumph.www;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.Regex;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.RegisterRequestBody;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.AuthService;
import site.neurotriumph.www.service.MailSenderService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserAlreadyExists() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      RegisterRequestBody registerRequestBody = new RegisterRequestBody(
        "valid@email.com",
        "Qwerty123");

      Mockito.doReturn(Optional.of(new User()))
        .when(userRepository)
        .findByEmail(registerRequestBody.getEmail());

      authService.register(registerRequestBody);
    });

    assertEquals(Message.USER_ALREADY_EXISTS, exception.getMessage());
  }

  @Test
  public void shouldAddUserAndSendEmail() {
    RegisterRequestBody registerRequestBody = new RegisterRequestBody(
      "valid@email.com",
      "Qwerty123");

    User user = new User(
      1L,
      registerRequestBody.getEmail(),
      registerRequestBody.getPassword(),
      false);

    Mockito.doReturn(user)
      .when(userRepository)
      .save(ArgumentMatchers.any(User.class));

    JWTCreator.Builder builder = Mockito.spy(JWT.create());
    MockedStatic<JWT> mockedStatic = Mockito.mockStatic(JWT.class, Mockito.CALLS_REAL_METHODS);
    Mockito.when(JWT.create())
      .thenReturn(builder);

    final ResultCollector<String> resultCollector = new ResultCollector<>();
    Mockito.doAnswer(resultCollector)
      .when(builder)
      .sign(ArgumentMatchers.any(Algorithm.class));

    authService.register(registerRequestBody);

    mockedStatic.close();

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

    assertDoesNotThrow(() -> {
      DecodedJWT decodedJWT = JWT.decode(token);

      assertNotNull(decodedJWT.getClaim(Field.USER_ID));
      assertNotNull(decodedJWT.getClaim(Field.EXPIRATION_TIME));

      assertEquals(user.getId(), decodedJWT.getClaim(Field.USER_ID).asLong());
      assertTrue(decodedJWT.getClaim(Field.EXPIRATION_TIME).asLong() > 0);
    });

    Mockito.verify(mailSenderService, Mockito.times(1))
      .send(
        ArgumentMatchers.eq(user.getEmail()),
        ArgumentMatchers.eq("Neuro Triumph"),
        ArgumentMatchers.matches(Regex.JWT_TOKEN));
  }
}
