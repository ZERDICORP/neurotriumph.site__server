package site.neurotriumph.www.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.entity.NeuralNetwork;
import site.neurotriumph.www.pojo.CreateNeuralNetworkRequestBody;
import site.neurotriumph.www.pojo.CreateNeuralNetworkResponseBody;
import site.neurotriumph.www.pojo.GetNeuralNetworkResponseBody;
import site.neurotriumph.www.repository.NeuralNetworkRepository;
import site.neurotriumph.www.repository.UserRepository;

@Service
public class NeuralNetworkService {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private NeuralNetworkRepository neuralNetworkRepository;

  public GetNeuralNetworkResponseBody get(Long id, Long userId) {
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

  public CreateNeuralNetworkResponseBody create(
    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody,
    Long id) {
    userRepository.findConfirmedById(id)
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    neuralNetworkRepository.findByNameAndOwnerId(createNeuralNetworkRequestBody.getName(), id)
      .ifPresent(o -> {
        throw new IllegalStateException(Message.NN_NAME_ALREADY_IN_USE);
      });

    neuralNetworkRepository.findByApiRootAndOwnerId(createNeuralNetworkRequestBody.getApi_root(), id)
      .ifPresent(o -> {
        throw new IllegalStateException(Message.NN_API_ROOT_ALREADY_IN_USE);
      });

    NeuralNetwork neuralNetwork = neuralNetworkRepository.save(new NeuralNetwork(
      id,
      createNeuralNetworkRequestBody.getName(),
      createNeuralNetworkRequestBody.getApi_root(),
      createNeuralNetworkRequestBody.getApi_secret()));

    return new CreateNeuralNetworkResponseBody(neuralNetwork.getId());
  }
}
