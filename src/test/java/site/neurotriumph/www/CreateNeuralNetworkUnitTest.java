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
import site.neurotriumph.www.pojo.CreateNeuralNetworkRequestBody;
import site.neurotriumph.www.pojo.CreateNeuralNetworkResponseBody;
import site.neurotriumph.www.repository.NeuralNetworkRepository;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.NeuralNetworkService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CreateNeuralNetworkUnitTest {
  @Autowired
  private NeuralNetworkService neuralNetworkService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private NeuralNetworkRepository neuralNetworkRepository;

  @Test
  public void shouldThrowIllegalStateExceptionBecauseNeuralNetworkApiRootAlreadyInUse() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
        "human_killer",
        "http://188.187.188.37:5000/v1/api",
        "123");

      User user = Mockito.spy(new User());
      user.setId(1L);

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      Mockito.when(neuralNetworkRepository.findByNameAndOwnerId(
          createNeuralNetworkRequestBody.getName(),
          user.getId()))
        .thenReturn(Optional.empty());

      Mockito.when(neuralNetworkRepository.findByApiRootAndOwnerId(
          createNeuralNetworkRequestBody.getApi_root(),
          user.getId()))
        .thenReturn(Optional.of(new NeuralNetwork()));

      neuralNetworkService.createNeuralNetwork(
        createNeuralNetworkRequestBody,
        user.getId());
    });

    assertEquals(Message.NN_API_ROOT_ALREADY_IN_USE, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseNeuralNetworkNameAlreadyInUse() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
        "human_killer",
        "http://188.187.188.37:5000/v1/api",
        "123");

      User user = Mockito.spy(new User());
      user.setId(1L);

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      Mockito.when(neuralNetworkRepository.findByNameAndOwnerId(
          createNeuralNetworkRequestBody.getName(),
          user.getId()))
        .thenReturn(Optional.of(new NeuralNetwork()));

      neuralNetworkService.createNeuralNetwork(
        createNeuralNetworkRequestBody,
        user.getId());
    });

    assertEquals(Message.NN_NAME_ALREADY_IN_USE, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
        "human_killer",
        "http://188.187.188.37:5000/v1/api",
        "123");

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(1L)))
        .thenReturn(Optional.empty());

      neuralNetworkService.createNeuralNetwork(
        createNeuralNetworkRequestBody,
        1L);
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldFindUserAndAddNeuralNetwork() {
    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      "human_killer",
      "http://188.187.188.37:5000/v1/api",
      "123");

    User user = Mockito.spy(new User());
    user.setId(1L);

    Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
      .thenReturn(Optional.of(user));

    Mockito.when(neuralNetworkRepository.findByNameAndOwnerId(
        createNeuralNetworkRequestBody.getName(),
        user.getId()))
      .thenReturn(Optional.empty());

    Mockito.when(neuralNetworkRepository.findByApiRootAndOwnerId(
        createNeuralNetworkRequestBody.getApi_root(),
        user.getId()))
      .thenReturn(Optional.empty());

    NeuralNetwork neuralNetwork = Mockito.spy(new NeuralNetwork());
    neuralNetwork.setId(1L);

    Mockito.when(neuralNetworkRepository.save(ArgumentMatchers.any(NeuralNetwork.class)))
      .thenReturn(neuralNetwork);

    CreateNeuralNetworkResponseBody createNeuralNetworkResponseBody = neuralNetworkService.createNeuralNetwork(
      createNeuralNetworkRequestBody,
      user.getId());

    assertNotNull(createNeuralNetworkResponseBody.getId());
    assertEquals(1L, (long) createNeuralNetworkResponseBody.getId());

    Mockito.verify(userRepository, Mockito.times(1))
      .findConfirmedById(ArgumentMatchers.eq(user.getId()));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findByNameAndOwnerId(
        createNeuralNetworkRequestBody.getName(),
        user.getId());

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findByApiRootAndOwnerId(
        createNeuralNetworkRequestBody.getApi_root(),
        user.getId());

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .save(ArgumentMatchers.any(NeuralNetwork.class));

    Mockito.verify(neuralNetwork, Mockito.times(1))
      .getId();
  }
}
