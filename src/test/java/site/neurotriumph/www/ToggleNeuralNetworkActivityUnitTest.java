package site.neurotriumph.www;

import java.util.Optional;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.entity.NeuralNetwork;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.ToggleNeuralNetworkActivityRequestBody;
import site.neurotriumph.www.repository.NeuralNetworkRepository;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.NeuralNetworkService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ToggleNeuralNetworkActivityUnitTest {
  @Autowired
  private NeuralNetworkService neuralNetworkService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private NeuralNetworkRepository neuralNetworkRepository;

  @Test
  public void shouldThrowIllegalStateExceptionBecauseNeuralNetworkDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody =
        new ToggleNeuralNetworkActivityRequestBody(1L);

      User user = Mockito.spy(new User());
      user.setId(1L);

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(1L, user.getId()))
        .thenReturn(Optional.empty());

      neuralNetworkService.toggleActivity(user.getId(), toggleNeuralNetworkActivityRequestBody);
    });

    assertEquals(Message.NN_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody =
        new ToggleNeuralNetworkActivityRequestBody(1L);

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(1L)))
        .thenReturn(Optional.empty());

      neuralNetworkService.toggleActivity(1L, toggleNeuralNetworkActivityRequestBody);
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldFindUserAndNeuralNetworkAndUpdateName() {
    ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody =
      new ToggleNeuralNetworkActivityRequestBody(1L);

    User user = Mockito.spy(new User());
    user.setId(1L);

    NeuralNetwork neuralNetwork = new NeuralNetwork();
    neuralNetwork.setApi_secret("123");
    neuralNetwork.setActive(true);

    NeuralNetwork spiedNeuralNetwork = Mockito.spy(neuralNetwork);

    Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
      .thenReturn(Optional.of(user));

    Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(
        toggleNeuralNetworkActivityRequestBody.getId(),
        user.getId()))
      .thenReturn(Optional.of(spiedNeuralNetwork));

    neuralNetworkService.toggleActivity(user.getId(), toggleNeuralNetworkActivityRequestBody);

    Mockito.verify(userRepository, Mockito.times(1))
      .findConfirmedById(ArgumentMatchers.eq(user.getId()));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findByIdAndOwnerId(
        toggleNeuralNetworkActivityRequestBody.getId(),
        user.getId());

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .setActive(ArgumentMatchers.eq(!neuralNetwork.isActive()));
  }
}
