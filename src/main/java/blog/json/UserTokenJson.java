package blog;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserTokenJson {
  public Integer id;
  @JsonFormat(shape = JsonFormat.Shape.STRING, 
              pattern = "yyyy-MM-dd HH:mm:ss")
  public Date expires;
	public String token;
  public String msg;
}
