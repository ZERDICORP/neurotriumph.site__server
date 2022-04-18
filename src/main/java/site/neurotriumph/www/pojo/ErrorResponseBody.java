package site.neurotriumph.www.pojo;

import lombok.Data;

@Data
public class ErrorResponseBody {
  private String error;

  public ErrorResponseBody(String error) {
    this.error = error;
  }
}
