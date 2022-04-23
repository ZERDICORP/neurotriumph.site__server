package site.neurotriumph.www.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import site.neurotriumph.www.constant.Const;
import site.neurotriumph.www.constant.Message;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordRequestBody {
  @NotNull(message = Message.PASSWORD_CANNOT_BE_BLANK)
  @Size(min = Const.MIN_PASSWORD_LENGTH, message = Message.PASSWORD_IS_TOO_SHORT)
  private String password;

  @NotNull(message = Message.PASSWORD_CANNOT_BE_BLANK)
  @Size(min = Const.MIN_PASSWORD_LENGTH, message = Message.PASSWORD_IS_TOO_SHORT)
  private String new_password;
}
