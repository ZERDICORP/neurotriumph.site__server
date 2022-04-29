package site.neurotriumph.www.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class GetNeuralNetworkResponseBody {
  private String name;
  private String api_root;
  private String api_secret;
  private boolean active;
  private int tests_passed;
  private int tests_failed;
}
