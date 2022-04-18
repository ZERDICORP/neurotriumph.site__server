package site.neurotriumph.www;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.RegisterRequestBody;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.AuthService;
import site.neurotriumph.www.service.MailSenderService;

import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RegisterUnitTest {
  @Autowired
  private AuthService authService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private MailSenderService mailSenderService;

  @Test(expected = IllegalStateException.class)
  public void shouldThrowIllegalStateExceptionError() {
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
      1L,
      registerRequestBody.getEmail(),
      registerRequestBody.getPassword(),
      false);

    Mockito.doReturn(user)
      .when(userRepository)
      .save(ArgumentMatchers.any(User.class));

    authService.register(registerRequestBody);

    Mockito.verify(userRepository, Mockito.times(1))
      .findByEmail(user.getEmail());

    Mockito.verify(userRepository, Mockito.times(1))
      .save(ArgumentMatchers.any(User.class));

    /*
     * Token Payload:
     * { "uid": 1 }
     * */
    final String expectedToken =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjF9.EXwYfVCKTHYYssS5ipKMgi49Mj_HU4GurKm0m8eWDqc";

    Mockito.verify(mailSenderService, Mockito.times(1))
      .send(
        ArgumentMatchers.eq(user.getEmail()),
        ArgumentMatchers.eq("Neuro Triumph"),
        ArgumentMatchers.eq(expectedToken));
  }
}
