package site.neurotriumph.www;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
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
import site.neurotriumph.www.constant.Regex;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.DeleteUserRequestBody;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.MailSenderService;
import site.neurotriumph.www.service.UserService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DeleteUserUnitTest {
  @Value("${app.secret}")
  private String appSecret;

  @Value("${spring.mail.username}")
  private String senderEmail;

  @Autowired
  private UserService userService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private MailSenderService mailSenderService;

  @Test
  public void shouldThrowIllegalStateExceptionBecauseWrongPassword() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      DeleteUserRequestBody deleteUserRequestBody = new DeleteUserRequestBody("123123");

      User user = Mockito.spy(new User(
        1L,
        senderEmail,
        DigestUtils.sha256Hex("Qwerty123"),
        true));

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      userService.deleteUser(user.getId(), deleteUserRequestBody);
    });

    assertEquals(Message.WRONG_PASSWORD, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      DeleteUserRequestBody deleteUserRequestBody = new DeleteUserRequestBody("Qwerty123");

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(1L)))
        .thenReturn(Optional.empty());

      userService.deleteUser(1L, deleteUserRequestBody);
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldFindUserAndSendEmail() {
    DeleteUserRequestBody deleteUserRequestBody = new DeleteUserRequestBody("Qwerty123");

    User user = Mockito.spy(new User(
      1L,
      senderEmail,
      DigestUtils.sha256Hex(deleteUserRequestBody.getPassword()),
      true));

    Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
      .thenReturn(Optional.of(user));

    JWTCreator.Builder builder = Mockito.spy(JWT.create());
    MockedStatic<JWT> mockedStatic = Mockito.mockStatic(JWT.class, Mockito.CALLS_REAL_METHODS);
    Mockito.when(JWT.create())
      .thenReturn(builder);

    final ResultCollector<String> resultCollector = new ResultCollector<>();
    Mockito.doAnswer(resultCollector)
      .when(builder)
      .sign(ArgumentMatchers.any(Algorithm.class));

    DeleteUserRequestBody spiedDeleteUserRequestBody = Mockito.spy(deleteUserRequestBody);

    userService.deleteUser(user.getId(), spiedDeleteUserRequestBody);

    mockedStatic.close();

    Mockito.verify(userRepository, Mockito.times(1))
      .findConfirmedById(user.getId());

    Mockito.verify(user, Mockito.times(1))
      .getPassword_hash();

    Mockito.verify(spiedDeleteUserRequestBody, Mockito.times(1))
      .getPassword();

    /*
     * Checking the token for validity.
     * */

    String token = resultCollector.getResult();

    assertDoesNotThrow(() -> {
      JWT.require(Algorithm.HMAC256(appSecret + TokenMarker.USER_DELETE_CONFIRMATION))
        .build()
        .verify(token);
    });

    assertDoesNotThrow(() -> {
      DecodedJWT decodedJWT = JWT.decode(token);

      assertNotNull(decodedJWT.getClaim(Field.EXPIRATION_TIME));
      assertTrue(decodedJWT.getClaim(Field.EXPIRATION_TIME).asLong() > 0);
    });

    Mockito.verify(mailSenderService, Mockito.times(1))
      .send(
        ArgumentMatchers.eq(user.getEmail()),
        ArgumentMatchers.eq("Neuro Triumph"),
        ArgumentMatchers.any(String.class));
  }
}
