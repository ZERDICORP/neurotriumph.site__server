package site.neurotriumph.www.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
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
}