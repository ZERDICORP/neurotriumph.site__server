package site.neurotriumph.www.pojo;

public class ErrorResponseBody {
  private String error;

  public ErrorResponseBody(String error) {
    this.error = error;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
