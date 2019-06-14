package blog;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostJson {
  public Integer id;
	public String title;
	public String text;
  @JsonFormat(shape = JsonFormat.Shape.STRING, 
              pattern = "yyyy-MM-dd HH:mm:ss")
	public Date date;
	public List<String> tags = new ArrayList<String>();
  public Integer user_id;
  public String user_name;
  public String msg;
}
