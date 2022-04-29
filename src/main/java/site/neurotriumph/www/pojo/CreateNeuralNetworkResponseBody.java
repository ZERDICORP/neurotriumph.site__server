package site.neurotriumph.www.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateNeuralNetworkResponseBody {
  private Long id;

  @JsonCreator
  public CreateNeuralNetworkResponseBody(Long id) {
    this.id = id;
  }
}
