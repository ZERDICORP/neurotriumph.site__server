package site.neurotriumph.www.entities;

import javax.persistence.*;

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
  private Boolean confirmed;

  public User() {}
  public User(Long id, String email, String password_hash, Boolean confirmed) {
    this.id = id;
    this.email = email;
    this.password_hash = password_hash;
    this.confirmed = confirmed;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword_hash() {
    return password_hash;
  }

  public void setPassword_hash(String password_hash) {
    this.password_hash = password_hash;
  }

  public Boolean getConfirmed() {
    return confirmed;
  }

  public void setConfirmed(Boolean confirmed) {
    this.confirmed = confirmed;
  }

  @Override
  public String toString() {
    return "User{" +
            "id=" + id +
            ", email='" + email + '\'' +
            ", password_hash='" + password_hash + '\'' +
            ", confirmed=" + confirmed +
            '}';
  }
}
