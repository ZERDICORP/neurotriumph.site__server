package site.neurotriumph.www.pojo;

import lombok.Data;
import site.neurotriumph.www.constant.Const;
import site.neurotriumph.www.constant.Message;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RegisterRequestBody {
  @NotEmpty(message = Message.EMAIL_CANNOT_BE_BLANK)
  @Email(message = Message.INVALID_EMAIL)
  private String email;

  @NotNull(message = Message.PASSWORD_CANNOT_BE_BLANK)
  @Size(min = Const.MIN_PASSWORD_LENGTH, message = Message.PASSWORD_IS_TOO_SHORT)
  private String password;

  public RegisterRequestBody(String email, String password) {
    this.email = email;
    this.password = password;
  }
}
