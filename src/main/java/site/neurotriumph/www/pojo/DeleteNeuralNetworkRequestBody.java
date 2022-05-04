package site.neurotriumph.www.pojo;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import site.neurotriumph.www.constant.Message;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DeleteNeuralNetworkRequestBody {
  @NotNull(message = Message.ID_CANNOT_BE_BLANK)
  @Min(value = 0, message = Message.INVALID_ID)
  private Long id;
}
