package site.neurotriumph.www.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.Regex;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@ToString
public class ConfirmationRequestBody {
  @NotNull(message = Message.TOKEN_CANNOT_BE_BLANK)
  @Pattern(regexp = Regex.JWT_TOKEN, message = Message.INVALID_TOKEN)
  private String token;

  @JsonCreator
  public ConfirmationRequestBody(String token) {
    this.token = token;
  }
}