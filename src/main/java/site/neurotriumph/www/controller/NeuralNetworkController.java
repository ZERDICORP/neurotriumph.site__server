package site.neurotriumph.www.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.neurotriumph.www.annotation.AuthTokenPayload;
import site.neurotriumph.www.annotation.WithAuthToken;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Regex;
import site.neurotriumph.www.pojo.CreateNeuralNetworkRequestBody;
import site.neurotriumph.www.pojo.CreateNeuralNetworkResponseBody;
import site.neurotriumph.www.pojo.GetNeuralNetworkResponseBody;
import site.neurotriumph.www.pojo.UpdateNeuralNetworkApiRootRequestBody;
import site.neurotriumph.www.pojo.UpdateNeuralNetworkNameRequestBody;
import site.neurotriumph.www.service.NeuralNetworkService;

import javax.validation.Valid;

@RestController
@Validated
public class NeuralNetworkController {
  @Autowired
  private NeuralNetworkService neuralNetworkService;

  @PutMapping("/user/nn/api_root")
  @WithAuthToken
  public void updateApiRoot(@AuthTokenPayload DecodedJWT authTokenPayload,
                            @Valid @RequestBody UpdateNeuralNetworkApiRootRequestBody
                              updateNeuralNetworkApiRootRequestBody) {
    neuralNetworkService.updateApiRoot(authTokenPayload.getClaim(Field.USER_ID).asLong(),
      updateNeuralNetworkApiRootRequestBody);
  }

  @PutMapping("/user/nn/name")
  @WithAuthToken
  public void updateName(@AuthTokenPayload DecodedJWT authTokenPayload,
                         @Valid @RequestBody UpdateNeuralNetworkNameRequestBody updateNeuralNetworkNameRequestBody) {
    neuralNetworkService.updateName(authTokenPayload.getClaim(Field.USER_ID).asLong(),
      updateNeuralNetworkNameRequestBody);
  }

  @GetMapping("/user/nn/{id:" + Regex.POSITIVE_INTEGER_NUMBER + "}")
  @WithAuthToken
  public GetNeuralNetworkResponseBody get(@AuthTokenPayload DecodedJWT authTokenPayload,
                                          @PathVariable Long id) {
    return neuralNetworkService.get(authTokenPayload.getClaim(Field.USER_ID).asLong(), id);
  }

  @PostMapping("/nn")
  @WithAuthToken
  public CreateNeuralNetworkResponseBody create(@AuthTokenPayload DecodedJWT authTokenPayload,
                                                @Valid @RequestBody CreateNeuralNetworkRequestBody
                                                  createNeuralNetworkRequestBody) {
    return neuralNetworkService.create(authTokenPayload.getClaim(Field.USER_ID).asLong(),
      createNeuralNetworkRequestBody);
  }
}
