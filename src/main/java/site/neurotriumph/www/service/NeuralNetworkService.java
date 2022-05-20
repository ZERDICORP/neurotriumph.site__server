package site.neurotriumph.www.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import site.neurotriumph.www.constant.Const;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.entity.NeuralNetwork;
import site.neurotriumph.www.pojo.CreateNeuralNetworkRequestBody;
import site.neurotriumph.www.pojo.CreateNeuralNetworkResponseBody;
import site.neurotriumph.www.pojo.DeleteNeuralNetworkRequestBody;
import site.neurotriumph.www.pojo.GetNeuralNetworkResponseBody;
import site.neurotriumph.www.pojo.GetUserNeuralNetworksResponseBodyItem;
import site.neurotriumph.www.pojo.ToggleNeuralNetworkActivityRequestBody;
import site.neurotriumph.www.pojo.UpdateNeuralNetworkApiRootRequestBody;
import site.neurotriumph.www.pojo.UpdateNeuralNetworkApiSecretRequestBody;
import site.neurotriumph.www.pojo.UpdateNeuralNetworkNameRequestBody;
import site.neurotriumph.www.repository.NeuralNetworkRepository;
import site.neurotriumph.www.repository.UserRepository;

@Service
public class NeuralNetworkService {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private NeuralNetworkRepository neuralNetworkRepository;

  public List<GetUserNeuralNetworksResponseBodyItem> getAllByUser(Long userId, Long page) {
    userRepository.findConfirmedById(userId)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    List<NeuralNetwork> neuralNetworks = neuralNetworkRepository.findAllByOwnerId(userId,
      PageRequest.of((int) (page * Const.NEURAL_NETWORKS_PAGE_SIZE), Const.NEURAL_NETWORKS_PAGE_SIZE));

    return neuralNetworks.stream()
      .map(n -> {
        long allTests = n.getTests_passed() + n.getTests_failed();
        return new GetUserNeuralNetworksResponseBodyItem(
          n.getId(),
          allTests > 0 ? (n.getTests_passed() / allTests * 100) : 0,
          n.getName(),
          n.isInvalid_api(),
          n.isActive());
      })
      .toList();
  }

  public void delete(Long userId, DeleteNeuralNetworkRequestBody deleteNeuralNetworkRequestBody) {
    userRepository.findConfirmedById(userId)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    NeuralNetwork neuralNetwork = neuralNetworkRepository.findByIdAndOwnerId(
        deleteNeuralNetworkRequestBody.getId(), userId)
      .orElseThrow(() -> new IllegalStateException(Message.NN_DOES_NOT_EXIST));

    neuralNetworkRepository.delete(neuralNetwork);
  }

  @Transactional
  public void toggleActivity(Long userId,
                             ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody) {
    userRepository.findConfirmedById(userId)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    NeuralNetwork neuralNetwork = neuralNetworkRepository.findByIdAndOwnerId(
        toggleNeuralNetworkActivityRequestBody.getId(), userId)
      .orElseThrow(() -> new IllegalStateException(Message.NN_DOES_NOT_EXIST));

    neuralNetwork.setActive(!neuralNetwork.isActive());
  }

  @Transactional
  public void updateApiSecret(Long userId,
                              UpdateNeuralNetworkApiSecretRequestBody updateNeuralNetworkApiSecretRequestBody) {
    userRepository.findConfirmedById(userId)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    NeuralNetwork neuralNetwork = neuralNetworkRepository.findByIdAndOwnerId(
        updateNeuralNetworkApiSecretRequestBody.getId(), userId)
      .orElseThrow(() -> new IllegalStateException(Message.NN_DOES_NOT_EXIST));

    if (neuralNetwork.getApi_secret().equals(
      updateNeuralNetworkApiSecretRequestBody.getNew_api_secret())) {
      throw new IllegalStateException(Message.NOTHING_TO_UPDATE);
    }

    neuralNetwork.setApi_secret(updateNeuralNetworkApiSecretRequestBody.getNew_api_secret());
  }

  @Transactional
  public void updateApiRoot(Long userId,
                            UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody) {
    userRepository.findConfirmedById(userId)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    NeuralNetwork neuralNetwork = neuralNetworkRepository.findByIdAndOwnerId(
        updateNeuralNetworkApiRootRequestBody.getId(), userId)
      .orElseThrow(() -> new IllegalStateException(Message.NN_DOES_NOT_EXIST));

    if (neuralNetwork.getApi_root().equals(
      updateNeuralNetworkApiRootRequestBody.getNew_api_root())) {
      throw new IllegalStateException(Message.NOTHING_TO_UPDATE);
    }

    neuralNetwork.setApi_root(updateNeuralNetworkApiRootRequestBody.getNew_api_root());
  }

  @Transactional
  public void updateName(Long userId, UpdateNeuralNetworkNameRequestBody updateNeuralNetworkNameRequestBody) {
    userRepository.findConfirmedById(userId)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    NeuralNetwork neuralNetwork = neuralNetworkRepository.findByIdAndOwnerId(
        updateNeuralNetworkNameRequestBody.getId(), userId)
      .orElseThrow(() -> new IllegalStateException(Message.NN_DOES_NOT_EXIST));

    if (neuralNetwork.getName().equals(
      updateNeuralNetworkNameRequestBody.getNew_name())) {
      throw new IllegalStateException(Message.NOTHING_TO_UPDATE);
    }

    neuralNetwork.setName(updateNeuralNetworkNameRequestBody.getNew_name());
  }

  public GetNeuralNetworkResponseBody get(Long userId, Long id) {
    userRepository.findConfirmedById(userId)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    NeuralNetwork neuralNetwork = neuralNetworkRepository.findByIdAndOwnerId(id, userId)
      .orElseThrow(() -> new IllegalStateException(Message.NN_DOES_NOT_EXIST));

    return new GetNeuralNetworkResponseBody(
      neuralNetwork.getName(),
      neuralNetwork.getApi_root(),
      neuralNetwork.getApi_secret(),
      neuralNetwork.isInvalid_api(),
      neuralNetwork.isActive(),
      neuralNetwork.getTests_passed(),
      neuralNetwork.getTests_failed());
  }

  public CreateNeuralNetworkResponseBody create(Long userId,
                                                CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody) {
    userRepository.findConfirmedById(userId)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    neuralNetworkRepository.findByNameAndOwnerId(createNeuralNetworkRequestBody.getName(), userId)
      .ifPresent(o -> {
        throw new IllegalStateException(Message.NN_NAME_ALREADY_IN_USE);
      });

    neuralNetworkRepository.findByApiRootAndOwnerId(createNeuralNetworkRequestBody.getApi_root(), userId)
      .ifPresent(o -> {
        throw new IllegalStateException(Message.NN_API_ROOT_ALREADY_IN_USE);
      });

    NeuralNetwork neuralNetwork = neuralNetworkRepository.save(new NeuralNetwork(
      userId,
      createNeuralNetworkRequestBody.getName(),
      createNeuralNetworkRequestBody.getApi_root(),
      createNeuralNetworkRequestBody.getApi_secret()));

    return new CreateNeuralNetworkResponseBody(neuralNetwork.getId());
  }
}
