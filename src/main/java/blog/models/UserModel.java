package blog;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "users")
public class UserModel implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;
	@Column(name = "name", unique = true, nullable = false)
	private String name;
	@Column(name = "password", nullable = false)
	private String password; // hashed
	@Column(name = "salt", nullable = false)
	private String salt;
  // This is here so hibernate doesn't freak out, but it isn't loaded.
  // Posts are obtained from PostRepo.
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, 
             orphanRemoval = true)
	private Set<PostModel> posts = new HashSet<PostModel>();

	public UserModel() {
    genNewSalt();
	}

  public UserModel(String name, String password) {
    this.name = name;
    genNewSalt();
    setPassword(password);
  }

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = Security.hash(password + salt);
	}

  public boolean isPasswordCorrect(String password) {
    return this.password.equals(Security.hash(password + salt));
  }

	public String getSalt() {
		return salt;
	}

  public void genNewSalt() {
    salt = Security.genToken(16);
  }

  /*
	public Set<PostModel> getPosts() {
		return posts;
	}
  */

  @Override
  public String toString() {
    //return "UserModel(" + id + ", " + name + ", " + posts + ")";
    return "UserModel(" + id + ", " + name + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UserModel that = (UserModel) o;
    return id == that.id; 
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
