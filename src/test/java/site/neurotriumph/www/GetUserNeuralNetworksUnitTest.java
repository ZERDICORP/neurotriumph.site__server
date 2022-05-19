package site.neurotriumph.www;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import site.neurotriumph.www.constant.Const;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.entity.NeuralNetwork;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.GetUserNeuralNetworksResponseBodyItem;
import site.neurotriumph.www.repository.NeuralNetworkRepository;
import site.neurotriumph.www.repository.UserRepository;
import site.neurotriumph.www.service.NeuralNetworkService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GetUserNeuralNetworksUnitTest {
  @Autowired
  private NeuralNetworkService neuralNetworkService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private NeuralNetworkRepository neuralNetworkRepository;

  @Test
  public void shouldThrowIllegalStateExceptionBecauseUserDoesNotExist() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(1L)))
        .thenReturn(Optional.empty());

      neuralNetworkService.getAllByUser(1L, 0L);
    });

    assertEquals(Message.USER_DOES_NOT_EXIST, exception.getMessage());
  }

  @Test
  public void shouldFindUserAndNeuralNetworksAndReturnSecondOne() {
    User user = Mockito.spy(new User());
    user.setId(1L);

    Pageable pageable = PageRequest.of(0, Const.NEURAL_NETWORKS_PAGE_SIZE);

    NeuralNetwork neuralNetwork = new NeuralNetwork();
    neuralNetwork.setName("human_killer");

    List<NeuralNetwork> neuralNetworks = new ArrayList<>();
    neuralNetworks.add(neuralNetwork);

    Mockito.when(userRepository.findConfirmedById(ArgumentMatchers.eq(user.getId())))
      .thenReturn(Optional.of(user));

    Mockito.when(neuralNetworkRepository.findAllByOwnerId(user.getId(), pageable))
      .thenReturn(neuralNetworks);

    List<GetUserNeuralNetworksResponseBodyItem> getUserNeuralNetworksResponseBodyItems =
      neuralNetworkService.getAllByUser(user.getId(), 0L);

    assertNotNull(getUserNeuralNetworksResponseBodyItems);
    assertEquals(1, getUserNeuralNetworksResponseBodyItems.size());

    GetUserNeuralNetworksResponseBodyItem getUserNeuralNetworksResponseBodyItem =
      getUserNeuralNetworksResponseBodyItems.get(0);

    assertNotNull(getUserNeuralNetworksResponseBodyItem);
    assertEquals(neuralNetwork.getName(), getUserNeuralNetworksResponseBodyItem.getName());

    Mockito.verify(userRepository, Mockito.times(1))
      .findConfirmedById(ArgumentMatchers.eq(user.getId()));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findAllByOwnerId(user.getId(), pageable);
  }
}
