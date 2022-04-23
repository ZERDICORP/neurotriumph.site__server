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

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      Mockito.doReturn(Optional.empty())
        .when(userRepository)
        .findConfirmedById(ArgumentMatchers.eq(1L));

      userService.getUser(1L);
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

    GetUserResponseBody getUserResponseBody = userService.getUser(1L);

    assertEquals(senderEmail, getUserResponseBody.getEmail());

    Mockito.verify(user, Mockito.times(1))
      .getEmail();
  }
}
