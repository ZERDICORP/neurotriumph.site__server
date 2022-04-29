package site.neurotriumph.www.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.Regex;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateNeuralNetworkRequestBody {
  @NotNull(message = Message.NN_NAME_CANNOT_BE_BLANK)
  @Pattern(regexp = Regex.NN_NAME, message =  Message.INVALID_NN_NAME)
  private String name;

  @NotEmpty(message = Message.NN_API_ROOT_CANNOT_BE_BLANK)
  @Pattern(regexp = Regex.URL, message = Message.INVALID_NN_API_ROOT)
  private String api_root;

  @NotEmpty(message = Message.NN_API_SECRET_CANNOT_BE_BLANK)
  private String api_secret;
}
