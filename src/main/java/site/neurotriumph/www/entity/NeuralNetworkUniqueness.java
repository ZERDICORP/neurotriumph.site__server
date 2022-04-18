package site.neurotriumph.www.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class NeuralNetworkUniqueness implements Serializable {
  private Long owner_id;
  private String name;

  public NeuralNetworkUniqueness(Long owner_id, String name) {
    this.owner_id = owner_id;
    this.name = name;
  }
}