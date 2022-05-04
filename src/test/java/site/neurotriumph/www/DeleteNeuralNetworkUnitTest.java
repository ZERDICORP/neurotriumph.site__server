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
import site.neurotriumph.www.pojo.DeleteNeuralNetworkRequestBody;
import site.neurotriumph.www.repository.NeuralNetworkRepository;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.NeuralNetworkService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DeleteNeuralNetworkUnitTest {
  @Autowired
  private NeuralNetworkService neuralNetworkService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private NeuralNetworkRepository neuralNetworkRepository;

  @Test
  public void shouldThrowIllegalStateExceptionBecauseNeuralNetworkDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      DeleteNeuralNetworkRequestBody deleteNeuralNetworkRequestBody =
        new DeleteNeuralNetworkRequestBody(1L);

      User user = Mockito.spy(new User());
      user.setId(1L);

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
        .thenReturn(Optional.of(user));

      Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(1L, user.getId()))
        .thenReturn(Optional.empty());

      neuralNetworkService.delete(user.getId(), deleteNeuralNetworkRequestBody);
    });

    assertEquals(Message.NN_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      DeleteNeuralNetworkRequestBody deleteNeuralNetworkRequestBody =
        new DeleteNeuralNetworkRequestBody(1L);

      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(1L)))
        .thenReturn(Optional.empty());

      neuralNetworkService.delete(1L, deleteNeuralNetworkRequestBody);
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldFindUserAndNeuralNetworkAndDeleteIt() {
    DeleteNeuralNetworkRequestBody deleteNeuralNetworkRequestBody =
      new DeleteNeuralNetworkRequestBody(1L);

    User user = new User();
    NeuralNetwork neuralNetwork = new NeuralNetwork();

    Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
      .thenReturn(Optional.of(user));

    Mockito.when(neuralNetworkRepository.findByIdAndOwnerId(
        deleteNeuralNetworkRequestBody.getId(),
        user.getId()))
      .thenReturn(Optional.of(neuralNetwork));

    neuralNetworkService.delete(user.getId(), deleteNeuralNetworkRequestBody);

    Mockito.verify(userRepository, Mockito.times(1))
      .findConfirmedById(ArgumentMatchers.eq(user.getId()));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findByIdAndOwnerId(
        deleteNeuralNetworkRequestBody.getId(),
        user.getId());

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .delete(ArgumentMatchers.eq(neuralNetwork));
  }
}
