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
import site.neurotriumph.www.entity.NeuralNetwork;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.UpdateNeuralNetworkApiSecretRequestBody;
import site.neurotriumph.www.repository.NeuralNetworkRepository;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.NeuralNetworkService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UpdateNeuralNetworkApiSecretUnitTest {
  @Autowired
  private NeuralNetworkService neuralNetworkService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private NeuralNetworkRepository neuralNetworkRepository;

  @Test
  public void shouldThrowIllegalStateExceptionBecauseNothingToUpdate() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      UpdateNeuralNetworkApiSecretRequestBody updateNeuralNetworkApiSecretRequestBody =
        new UpdateNeuralNetworkApiSecretRequestBody(1L, "1234");

      User user = Mockito.spy(new User());
      user.setId(1L);

      NeuralNetwork neuralNetwork = new NeuralNetwork();
      neuralNetwork.setApi_secret("1234");

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(1L, user.getId()))
        .thenReturn(Optional.of(neuralNetwork));

      neuralNetworkService.updateApiSecret(user.getId(), updateNeuralNetworkApiSecretRequestBody);
    });

    assertEquals(Message.NOTHING_TO_UPDATE, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseNeuralNetworkDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      UpdateNeuralNetworkApiSecretRequestBody updateNeuralNetworkApiSecretRequestBody =
        new UpdateNeuralNetworkApiSecretRequestBody(1L, "1234");

      User user = Mockito.spy(new User());
      user.setId(1L);

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(1L, user.getId()))
        .thenReturn(Optional.empty());

      neuralNetworkService.updateApiSecret(user.getId(), updateNeuralNetworkApiSecretRequestBody);
    });

    assertEquals(Message.NN_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      UpdateNeuralNetworkApiSecretRequestBody updateNeuralNetworkApiSecretRequestBody =
        new UpdateNeuralNetworkApiSecretRequestBody(1L, "1234");

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(1L)))
        .thenReturn(Optional.empty());

      neuralNetworkService.updateApiSecret(1L, updateNeuralNetworkApiSecretRequestBody);
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldFindUserAndNeuralNetworkAndUpdateName() {
    UpdateNeuralNetworkApiSecretRequestBody updateNeuralNetworkApiSecretRequestBody =
      new UpdateNeuralNetworkApiSecretRequestBody(1L, "1234");

    User user = Mockito.spy(new User());
    user.setId(1L);

    NeuralNetwork neuralNetwork = new NeuralNetwork();
    neuralNetwork.setApi_secret("123");

    NeuralNetwork spiedNeuralNetwork = Mockito.spy(neuralNetwork);

    Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
      .thenReturn(Optional.of(user));

    Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(
        updateNeuralNetworkApiSecretRequestBody.getId(),
        user.getId()))
      .thenReturn(Optional.of(spiedNeuralNetwork));

    neuralNetworkService.updateApiSecret(user.getId(), updateNeuralNetworkApiSecretRequestBody);

    Mockito.verify(userRepository, Mockito.times(1))
      .findConfirmedById(ArgumentMatchers.eq(user.getId()));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findByIdAndOwnerId(
        updateNeuralNetworkApiSecretRequestBody.getId(),
        user.getId());

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .getApi_secret();

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .setApi_secret(ArgumentMatchers.eq(updateNeuralNetworkApiSecretRequestBody.getNew_api_secret()));
  }
}
