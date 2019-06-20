package blog;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostListJson {
	public List<PostJson> posts;
  public String msg;
}
