package blog;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.Calendar;
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
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "posts")
public class PostModel implements java.io.Serializable {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;
	@Column(name = "title", nullable = false)
	private String title;
	@Column(name = "post_text", nullable = false)
	private String post_text;
  @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "post_date", nullable = false)
	private Date post_date;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserModel user;
	@ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name="post_tags", 
    joinColumns=@JoinColumn(name="post_id", referencedColumnName="id"),
    inverseJoinColumns=@JoinColumn(name="tag_id", referencedColumnName="id"))
	private Set<TagModel> tags = new HashSet<TagModel>();

	public PostModel() {
    this.post_date = Calendar.getInstance().getTime();
	}

  public PostModel(String title, String text) {
    this();
    this.title = title;
    this.post_text = text;
  }

  public PostModel(String title, String text, UserModel user) {
    this(title, text);
    this.user = user;
  }

  public PostModel(String title, String text, UserModel user, 
                   Iterable<TagModel> tags) {
    this(title, text, user);
    addTags(tags);
  }


	public Integer getId() {
		return this.id;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPostText() {
		return this.post_text;
	}

	public void setPostText(String post_text) {
		this.post_text = post_text;
	}

	public Date getPostDate() {
		return this.post_date;
	}

	public void setPostDate(Date post_date) {
		this.post_date = post_date;
	}

  public UserModel getUser() {
    return user;
  }

  public void setUser(UserModel user) {
    this.user = user;
  }

	public Set<TagModel> getTags() {
		return tags;
	}

  public void addTag(TagModel tag) {
    tags.add(tag);
  }

  public void addTags(Iterable<TagModel> tags) {
    for (var t : tags) {
      this.tags.add(t);
    }
  }

  @Override
  public String toString() {
    return "PostModel(" + title + ", " + post_text + ", " + post_date +
           ", " + tags + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PostModel that = (PostModel) o;
    return id == that.id;   
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
