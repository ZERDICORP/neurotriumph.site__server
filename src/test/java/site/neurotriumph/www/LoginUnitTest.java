package site.neurotriumph.www;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.codec.digest.DigestUtils;
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
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.LoginRequestBody;
import site.neurotriumph.www.pojo.LoginResponseBody;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.AuthService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LoginUnitTest {
  @Value("${app.secret}")
  private String appSecret;

  @Value("${spring.mail.username}")
  private String senderEmail;

  @Autowired
  private AuthService authService;

  @MockBean
  private UserRepository userRepository;

  private final LoginRequestBody loginRequestBody = new LoginRequestBody(null, "Qwerty123");

  @Test
  public void shouldThrowIllegalStateExceptionBecauseWrongPassword() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      User user = new User();
      user.setConfirmed(true);
      user.setPassword_hash("123");

      Mockito.doReturn(Optional.of(user))
        .when(userRepository)
        .findByEmail(ArgumentMatchers.eq(loginRequestBody.getEmail()));

      authService.login(loginRequestBody);
    });

    assertEquals(Message.WRONG_PASSWORD, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserNotConfirmed() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      Mockito.doReturn(Optional.of(new User()))
        .when(userRepository)
        .findByEmail(ArgumentMatchers.eq(loginRequestBody.getEmail()));

      authService.login(loginRequestBody);
    });

    assertEquals(Message.USER_NOT_CONFIRMED, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      Mockito.doReturn(Optional.empty())
        .when(userRepository)
        .findByEmail(ArgumentMatchers.eq(loginRequestBody.getEmail()));

      authService.login(loginRequestBody);
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldCreateAuthTokenAndReturnLoginResponseBody() {
    User user = Mockito.spy(new User(1L, null,
      DigestUtils.sha256Hex(loginRequestBody.getPassword()), true));

    Mockito.doReturn(Optional.of(user))
      .when(userRepository)
      .findByEmail(ArgumentMatchers.eq(loginRequestBody.getEmail()));

    JWTCreator.Builder builder = Mockito.spy(JWT.create());
    MockedStatic<JWT> mockedStatic = Mockito.mockStatic(JWT.class, Mockito.CALLS_REAL_METHODS);
    Mockito.when(JWT.create())
      .thenReturn(builder);

    final ResultCollector<String> resultCollector = new ResultCollector<>();
    Mockito.doAnswer(resultCollector)
      .when(builder)
      .sign(ArgumentMatchers.any(Algorithm.class));

    LoginResponseBody loginResponseBody = authService.login(loginRequestBody);

    mockedStatic.close();

    validateToken(loginResponseBody.getToken(), user.getId());
    assertEquals(user.getId(), loginResponseBody.getUid());

    Mockito.verify(user, Mockito.times(1))
      .isConfirmed();

    Mockito.verify(user, Mockito.times(1))
      .getPassword_hash();

    validateToken(resultCollector.getResult(), user.getId());
  }

  private void validateToken(String token, Long expected_user_id) {
    assertDoesNotThrow(() -> {
      JWT.require(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION))
        .build()
        .verify(token);
    });

    assertDoesNotThrow(() -> {
      DecodedJWT decodedJWT = JWT.decode(token);

      Claim actual_user_id = decodedJWT.getClaim(Field.USER_ID);
      Claim expiration_time = decodedJWT.getClaim(Field.EXPIRATION_TIME);

      assertNotNull(actual_user_id);
      assertNotNull(expiration_time);

      assertEquals(expected_user_id, actual_user_id.asLong());

      assertTrue(expiration_time.asLong() > 0);
    });
  }
}
