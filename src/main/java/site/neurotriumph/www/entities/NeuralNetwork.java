package site.neurotriumph.www.entities;

import javax.persistence.*;

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
  private Boolean active;

  public NeuralNetwork() {}
  public NeuralNetwork(Long owner_id, String name, String api_root, String api_secret) {
    this.owner_id = owner_id;
    this.name = name;
    this.api_root = api_root;
    this.api_secret = api_secret;
  }

  public Long getOwner_id() {
    return owner_id;
  }

  public void setOwner_id(Long owner_id) {
    this.owner_id = owner_id;
  }

  public String getApi_secret() {
    return api_secret;
  }

  public void setApi_secret(String api_secret) {
    this.api_secret = api_secret;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getApi_root() {
    return api_root;
  }

  public void setApi_root(String api_root) {
    this.api_root = api_root;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  @Override
  public String toString() {
    return "NeuralNetwork{" +
            "id=" + id +
            ", owner_id=" + owner_id +
            ", name='" + name + '\'' +
            ", api_root='" + api_root + '\'' +
            ", api_secret='" + api_secret + '\'' +
            ", active=" + active +
            '}';
  }
}