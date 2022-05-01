package site.neurotriumph.www.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.entity.NeuralNetwork;
import site.neurotriumph.www.pojo.CreateNeuralNetworkRequestBody;
import site.neurotriumph.www.pojo.CreateNeuralNetworkResponseBody;
import site.neurotriumph.www.pojo.GetNeuralNetworkResponseBody;
import site.neurotriumph.www.pojo.UpdateNeuralNetworkApiRootRequestBody;
import site.neurotriumph.www.pojo.UpdateNeuralNetworkNameRequestBody;
import site.neurotriumph.www.repository.NeuralNetworkRepository;
import site.neurotriumph.www.repository.UserRepository;

import javax.transaction.Transactional;

@Service
public class NeuralNetworkService {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private NeuralNetworkRepository neuralNetworkRepository;

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
