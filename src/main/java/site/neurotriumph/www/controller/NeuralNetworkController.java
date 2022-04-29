package site.neurotriumph.www.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.neurotriumph.www.annotation.WithAuthToken;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Regex;
import site.neurotriumph.www.pojo.CreateNeuralNetworkRequestBody;
import site.neurotriumph.www.pojo.CreateNeuralNetworkResponseBody;
import site.neurotriumph.www.pojo.GetNeuralNetworkResponseBody;
import site.neurotriumph.www.service.NeuralNetworkService;

import javax.validation.Valid;

@RestController
@Validated
public class NeuralNetworkController {
  @Autowired
  private NeuralNetworkService neuralNetworkService;

  @GetMapping("/user/nn/{id:" + Regex.POSITIVE_INTEGER_NUMBER + "}")
  @WithAuthToken
  public GetNeuralNetworkResponseBody getNeuralNetwork(@PathVariable Long id, DecodedJWT decodedJWT) {
    return neuralNetworkService.get(id, decodedJWT.getClaim(Field.USER_ID).asLong());
  }

  @PostMapping("/nn")
  @WithAuthToken
  public CreateNeuralNetworkResponseBody createNeuralNetwork(
    @Valid @RequestBody CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody,
    DecodedJWT decodedJWT) {
    return neuralNetworkService.create(createNeuralNetworkRequestBody,
      decodedJWT.getClaim(Field.USER_ID).asLong());
  }
}
