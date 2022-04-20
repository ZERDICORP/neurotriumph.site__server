package site.neurotriumph.www.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
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

  public User(String email, String password_hash) {
    this.email = email;
    this.password_hash = password_hash;
  }
}
