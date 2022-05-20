package site.neurotriumph.www;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import site.neurotriumph.www.constant.Const;
import site.neurotriumph.www.entity.NeuralNetwork;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.GetNeuralNetworksResponseBodyItem;
import site.neurotriumph.www.repository.NeuralNetworkRepository;
import site.neurotriumph.www.service.NeuralNetworkService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GetNeuralNetworksUnitTest {
  @Autowired
  private NeuralNetworkService neuralNetworkService;

  @MockBean
  private NeuralNetworkRepository neuralNetworkRepository;

  @Test
  public void shouldFindNeuralNetworksAndReturnIt() {
    User user = Mockito.spy(new User());
    user.setId(1L);

    Pageable pageable = PageRequest.of(0, Const.NEURAL_NETWORKS_PAGE_SIZE);

    NeuralNetwork neuralNetwork = new NeuralNetwork();
    neuralNetwork.setName("human_killer");

    List<NeuralNetwork> neuralNetworks = new ArrayList<>();
    neuralNetworks.add(neuralNetwork);

    Mockito.when(neuralNetworkRepository.findAllActive(pageable))
      .thenReturn(neuralNetworks);

    List<GetNeuralNetworksResponseBodyItem> getNeuralNetworksResponseBodyItems =
      neuralNetworkService.getAll(0L);

    assertNotNull(getNeuralNetworksResponseBodyItems);
    assertEquals(1, getNeuralNetworksResponseBodyItems.size());

    GetNeuralNetworksResponseBodyItem getUserNeuralNetworksResponseBodyItem =
      getNeuralNetworksResponseBodyItems.get(0);

    assertNotNull(getUserNeuralNetworksResponseBodyItem);
    assertEquals(neuralNetwork.getName(), getUserNeuralNetworksResponseBodyItem.getName());

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findAllActive(pageable);
  }
}
