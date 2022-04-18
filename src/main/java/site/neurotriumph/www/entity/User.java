package site.neurotriumph.www.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 320, nullable = false, unique = true)
  private String email;

  @Column(length = 64, nullable = false)
  private String password_hash;

  @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
  private boolean confirmed;

  public User() {
  }

  public User(String email, String password_hash) {
    this.email = email;
    this.password_hash = password_hash;
  }

  public User(Long id, String email, String password_hash, Boolean confirmed) {
    this.id = id;
    this.email = email;
    this.password_hash = password_hash;
    this.confirmed = confirmed;
  }
}
