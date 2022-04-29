package site.neurotriumph.www.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.neurotriumph.www.annotation.WithAuthToken;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.pojo.CreateNeuralNetworkRequestBody;
import site.neurotriumph.www.pojo.CreateNeuralNetworkResponseBody;
import site.neurotriumph.www.service.NeuralNetworkService;

import javax.validation.Valid;

@RestController
@Validated
public class NeuralNetworkController {
  @Autowired
  private NeuralNetworkService neuralNetworkService;

  @PostMapping("/nn")
  @WithAuthToken
  public CreateNeuralNetworkResponseBody createNeuralNetwork(
    @Valid @RequestBody CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody,
    DecodedJWT decodedJWT) {
    return neuralNetworkService.createNeuralNetwork(createNeuralNetworkRequestBody,
      decodedJWT.getClaim(Field.USER_ID).asLong());
  }
}
