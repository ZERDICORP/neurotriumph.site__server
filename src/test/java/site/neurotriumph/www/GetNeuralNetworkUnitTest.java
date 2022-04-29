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
import site.neurotriumph.www.pojo.GetNeuralNetworkResponseBody;
import site.neurotriumph.www.repository.NeuralNetworkRepository;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.NeuralNetworkService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GetNeuralNetworkUnitTest {
  @Autowired
  private NeuralNetworkService neuralNetworkService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private NeuralNetworkRepository neuralNetworkRepository;

  @Test
  public void shouldThrowIllegalStateExceptionBecauseNeuralNetworkDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      User user = Mockito.spy(new User());
      user.setId(1L);

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(1L, user.getId()))
        .thenReturn(Optional.empty());

      neuralNetworkService.get(1L, user.getId());
    });

    assertEquals(Message.NN_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(1L)))
        .thenReturn(Optional.empty());

      neuralNetworkService.get(1L, 1L);
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldFindUserAndNeuralNetworkAndReturnSecondOne() {
    User user = Mockito.spy(new User());
    user.setId(1L);

    NeuralNetwork neuralNetwork = new NeuralNetwork(
      user.getId(),
      "human_killer",
      "http://188.187.188.37:5000/v1/api",
      "123");
    neuralNetwork.setActive(true);

    NeuralNetwork spiedNeuralNetwork = Mockito.spy(neuralNetwork);

    Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
      .thenReturn(Optional.of(user));

    Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(
        neuralNetwork.getId(),
        user.getId()))
      .thenReturn(Optional.of(spiedNeuralNetwork));

    GetNeuralNetworkResponseBody getNeuralNetworkResponseBody = neuralNetworkService.get(
      neuralNetwork.getId(),
      user.getId());

    assertNotNull(getNeuralNetworkResponseBody);
    assertEquals(neuralNetwork.getName(), getNeuralNetworkResponseBody.getName());
    assertEquals(neuralNetwork.getApi_root(),
      getNeuralNetworkResponseBody.getApi_root());
    assertEquals(neuralNetwork.getApi_secret(), getNeuralNetworkResponseBody.getApi_secret());
    assertTrue(getNeuralNetworkResponseBody.isActive());
    assertEquals(neuralNetwork.getTests_passed(), getNeuralNetworkResponseBody.getTests_passed());
    assertEquals(neuralNetwork.getTests_failed(), getNeuralNetworkResponseBody.getTests_failed());

    Mockito.verify(userRepository, Mockito.times(1))
      .findConfirmedById(ArgumentMatchers.eq(user.getId()));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findByIdAndOwnerId(
        neuralNetwork.getId(),
        user.getId());

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .getName();

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .getApi_root();

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .getApi_secret();

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .isActive();

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .getTests_passed();

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .getTests_failed();
  }
}
