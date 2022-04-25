package site.neurotriumph.www;

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
import site.neurotriumph.www.service.UserService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConfirmEmailUpdateUnitTest {
  @Autowired
  private UserService userService;

  @MockBean
  private UserRepository userRepository;

  @Test
  public void shouldThrowIllegalStateExceptionBecauseNothingToUpdate() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      User user = Mockito.spy(new User());
      user.setId(1L);
      user.setEmail("test@gmail.com");

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      String newEmail = "test@gmail.com";

      userService.confirmEmailUpdate(user.getId(), newEmail);
    });

    assertEquals(Message.NOTHING_TO_UPDATE, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(1L)))
        .thenReturn(Optional.empty());

      userService.confirmEmailUpdate(1L, "new_email@gmail.com");
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldUpdateEmail() {
    User user = Mockito.spy(new User());
    user.setId(1L);
    user.setEmail("test@gmail.com");

    Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
      .thenReturn(Optional.of(user));

    String newEmail = "new_email@gmail.com";

    userService.confirmEmailUpdate(user.getId(), newEmail);

    Mockito.verify(user, Mockito.times(1))
      .getEmail();

    Mockito.verify(user, Mockito.times(1))
      .setEmail(newEmail);
  }
}
