package site.neurotriumph.www;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.AuthService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConfirmRegistrationUnitTest {
  @Autowired
  private AuthService authService;

  @MockBean
  private UserRepository userRepository;

  private final DecodedJWT decodedJWT = JWT.decode(
    /*
     * Token Payload:
     * { "uid": 1 }
     * */
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjF9.1hF426wB0xOtHGdBwzgDo2LNs91fw5yF3tZ91aEqVyg");

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserAlreadyConfirmed() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      User user = Mockito.spy(new User());
      user.setConfirmed(true);

      Mockito.doReturn(Optional.of(user))
        .when(userRepository)
        .findById(ArgumentMatchers.eq(1L));

      authService.confirmRegistration(decodedJWT);

      Mockito.verify(user, Mockito.times(1))
        .isConfirmed();
    });

    assertEquals(Message.USER_ALREADY_CONFIRMED, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      Mockito.doReturn(Optional.empty())
        .when(userRepository)
        .findById(ArgumentMatchers.eq(1L));

      authService.confirmRegistration(decodedJWT);
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldConfirmUser() {
    User user = Mockito.spy(new User());

    Mockito.doReturn(Optional.of(user))
      .when(userRepository)
      .findById(ArgumentMatchers.eq(1L));

    authService.confirmRegistration(decodedJWT);

    Mockito.verify(user, Mockito.times(1))
      .setConfirmed(ArgumentMatchers.eq(true));
  }
}
