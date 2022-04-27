package site.neurotriumph.www;

import org.apache.commons.codec.digest.DigestUtils;
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
public class ConfirmPasswordUpdateUnitTest {
  @Autowired
  private UserService userService;

  @MockBean
  private UserRepository userRepository;

  @Test
  public void shouldThrowIllegalStateExceptionBecauseNothingToUpdate() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      User user = Mockito.spy(new User());
      user.setId(1L);
      user.setPassword_hash(DigestUtils.sha256Hex("Qwerty123"));

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      String newPasswordHash = DigestUtils.sha256Hex("Qwerty123");

      userService.confirmPasswordUpdate(user.getId(), newPasswordHash);
    });

    assertEquals(Message.NOTHING_TO_UPDATE, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(1L)))
        .thenReturn(Optional.empty());

      userService.confirmPasswordUpdate(1L, DigestUtils.sha256Hex("Qwerty1234"));
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldUpdatePassword() {
    User user = Mockito.spy(new User());
    user.setId(1L);
    user.setPassword_hash(DigestUtils.sha256Hex("Qwerty123"));

    Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
      .thenReturn(Optional.of(user));

    String newPasswordHash = DigestUtils.sha256Hex("Qwerty1234");

    userService.confirmPasswordUpdate(user.getId(), newPasswordHash);

    Mockito.verify(userRepository, Mockito.times(1))
      .findConfirmedById(ArgumentMatchers.eq(user.getId()));

    Mockito.verify(user, Mockito.times(1))
      .getPassword_hash();

    Mockito.verify(user, Mockito.times(1))
      .setPassword_hash(newPasswordHash);
  }
}
