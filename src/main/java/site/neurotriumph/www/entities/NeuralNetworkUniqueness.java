package site.neurotriumph.www.entities;

import java.io.Serializable;

public class NeuralNetworkUniqueness implements Serializable {
  private Long owner_id;
  private String name;

  public NeuralNetworkUniqueness(Long owner_id, String name) {
    this.owner_id = owner_id;
    this.name = name;
  }
}