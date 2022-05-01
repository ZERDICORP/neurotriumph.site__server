package site.neurotriumph.www.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.Regex;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNeuralNetworkApiRootRequestBody {
  @NotNull(message = Message.ID_CANNOT_BE_BLANK)
  @Min(value = 0, message = Message.INVALID_ID)
  private Long id;

  @NotEmpty(message = Message.NN_API_ROOT_CANNOT_BE_BLANK)
  @Pattern(regexp = Regex.URL, message = Message.INVALID_NN_API_ROOT)
  private String new_api_root;
}
