package site.neurotriumph.www;

import com.auth0.jwt.JWT;
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
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.GetUserResponseBody;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.UserService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GetUserUnitTest {
  @Value("app.secret")
  private String appSecret;

  @Value("${spring.mail.username}")
  private String senderEmail;

  @Autowired
  private UserService userService;

  @MockBean
  private UserRepository userRepository;

  private final DecodedJWT decodedJWT = JWT.decode(
    /*
     * Token Payload:
     * { "uid": 1 }
     * */
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjF9.aVbN7ZpGSe_7q4bvCxPX0Ahur6Uas0mb3ZDgnGmUpU0");

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      Mockito.doReturn(Optional.empty())
        .when(userRepository)
        .findConfirmedById(ArgumentMatchers.eq(1L));

      userService.getUser(decodedJWT);
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldReturnGetUserResponseBody() {
    User user = Mockito.spy(new User());
    user.setEmail(senderEmail);

    Mockito.doReturn(Optional.of(user))
      .when(userRepository)
      .findConfirmedById(ArgumentMatchers.eq(1L));

    GetUserResponseBody getUserResponseBody = userService.getUser(decodedJWT);

    assertEquals(senderEmail, getUserResponseBody.getEmail());

    Mockito.verify(user, Mockito.times(1))
      .getEmail();
  }
}
