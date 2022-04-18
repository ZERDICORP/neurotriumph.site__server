package site.neurotriumph.www.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
@IdClass(NeuralNetworkUniqueness.class)
public class NeuralNetwork {
  @Id
  @Column(columnDefinition = "BIGINT(20) UNIQUE AUTO_INCREMENT", nullable = false)
  private Long id;

  @Id
  @Column(nullable = false)
  private Long owner_id;

  @Id
  @Column(nullable = false)
  private String name;

  @Column(length = 2048, nullable = false)
  private String api_root;

  @Column(nullable = false)
  private String api_secret;

  @Column(columnDefinition = "TINYINT(1) DEFAULT 1", nullable = false)
  private boolean active;

  public NeuralNetwork() {}
  public NeuralNetwork(Long owner_id, String name, String api_root, String api_secret) {
    this.owner_id = owner_id;
    this.name = name;
    this.api_root = api_root;
    this.api_secret = api_secret;
  }
}