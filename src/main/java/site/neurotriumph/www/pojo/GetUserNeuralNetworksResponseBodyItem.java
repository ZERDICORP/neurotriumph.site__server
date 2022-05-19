package site.neurotriumph.www.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class GetUserNeuralNetworksResponseBodyItem {
  private Long id;
  private Long coeff;
  private String name;
  private boolean invalid_api;
  private boolean active;
}
