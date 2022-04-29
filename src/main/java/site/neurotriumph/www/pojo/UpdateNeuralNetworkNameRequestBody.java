package site.neurotriumph.www.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.Regex;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNeuralNetworkNameRequestBody {
  @NotNull(message = Message.ID_CANNOT_BE_BLANK)
  @Min(value = 0, message = Message.INVALID_ID)
  private Long id;

  @NotNull(message = Message.NN_NAME_CANNOT_BE_BLANK)
  @Pattern(regexp = Regex.NN_NAME, message =  Message.INVALID_NN_NAME)
  private String new_name;
}
