package blog;

import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tags")
public class TagModel implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;
	@Column(name = "name", unique = true, nullable = false)
	private String name;

	public TagModel() {
	}

  public TagModel(String name) {
    this.name = name;
  }

	public Integer getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

  @Override
  public String toString() {
    return "TagModel(" +  id + ", " + name + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TagModel that = (TagModel) o;
    return id == that.id;  
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
