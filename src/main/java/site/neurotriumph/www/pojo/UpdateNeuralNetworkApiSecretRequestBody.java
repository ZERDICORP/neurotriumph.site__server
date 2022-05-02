package site.neurotriumph.www.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import site.neurotriumph.www.constant.Message;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNeuralNetworkApiSecretRequestBody {
  @NotNull(message = Message.ID_CANNOT_BE_BLANK)
  @Min(value = 0, message = Message.INVALID_ID)
  private Long id;

  @NotEmpty(message = Message.NN_API_SECRET_CANNOT_BE_BLANK)
  private String new_api_secret;
}
