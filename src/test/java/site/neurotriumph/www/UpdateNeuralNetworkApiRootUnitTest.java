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
import site.neurotriumph.www.pojo.UpdateNeuralNetworkApiRootRequestBody;
import site.neurotriumph.www.repository.NeuralNetworkRepository;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.NeuralNetworkService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UpdateNeuralNetworkApiRootUnitTest {
  @Autowired
  private NeuralNetworkService neuralNetworkService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private NeuralNetworkRepository neuralNetworkRepository;

  @Test
  public void shouldThrowIllegalStateExceptionBecauseNothingToUpdate() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
        new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v2/api");

      User user = Mockito.spy(new User());
      user.setId(1L);

      NeuralNetwork neuralNetwork = new NeuralNetwork();
      neuralNetwork.setApi_root("http://188.187.188.37:5000/v2/api");

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(1L, user.getId()))
        .thenReturn(Optional.of(neuralNetwork));

      neuralNetworkService.updateApiRoot(user.getId(), updateNeuralNetworkApiRootRequestBody);
    });

    assertEquals(Message.NOTHING_TO_UPDATE, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseNeuralNetworkDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
        new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v2/api");

      User user = Mockito.spy(new User());
      user.setId(1L);

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(1L, user.getId()))
        .thenReturn(Optional.empty());

      neuralNetworkService.updateApiRoot(user.getId(), updateNeuralNetworkApiRootRequestBody);
    });

    assertEquals(Message.NN_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
        new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v2/api");

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(1L)))
        .thenReturn(Optional.empty());

      neuralNetworkService.updateApiRoot(1L, updateNeuralNetworkApiRootRequestBody);
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldFindUserAndNeuralNetworkAndUpdateName() {
    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v2/api");

    User user = Mockito.spy(new User());
    user.setId(1L);

    NeuralNetwork neuralNetwork = new NeuralNetwork();
    neuralNetwork.setApi_root("http://188.187.188.37:5000/v1/api");

    NeuralNetwork spiedNeuralNetwork = Mockito.spy(neuralNetwork);

    Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
      .thenReturn(Optional.of(user));

    Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(
        updateNeuralNetworkApiRootRequestBody.getId(),
        user.getId()))
      .thenReturn(Optional.of(spiedNeuralNetwork));

    neuralNetworkService.updateApiRoot(user.getId(), updateNeuralNetworkApiRootRequestBody);

    Mockito.verify(userRepository, Mockito.times(1))
      .findConfirmedById(ArgumentMatchers.eq(user.getId()));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findByIdAndOwnerId(
        updateNeuralNetworkApiRootRequestBody.getId(),
        user.getId());

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .getApi_root();

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .setApi_root(ArgumentMatchers.eq(updateNeuralNetworkApiRootRequestBody.getNew_api_root()));
  }
}
