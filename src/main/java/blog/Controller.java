package blog;

import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Calendar;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


@RestController
public class Controller {
  @Autowired
  private UserRepo user_repo;
  @Autowired
  private PostRepo post_repo;
  @Autowired
  private TagRepo tag_repo;

  // defined in application.properties
  @Value("${kame.disable_create_user}")
  private boolean disable_create_user = false;
  @Value("${kame.secret_key}") 
  private String secret_key;
  @Value("${kame.session_valid_days}") 
  private int session_valid_days;

  private static 
  PostJson PostModelToJson(PostModel post, boolean include_user) {
    var json_post = new PostJson();
    json_post.id = post.getId();
    json_post.title = post.getTitle();
    json_post.text = post.getPostText();
    json_post.date = post.getPostDate();
    json_post.tags = post.getTags().stream().map(t -> t.getName())
                     .collect(Collectors.toList());

    if (include_user) {
      json_post.user_id = post.getUser().getId();
      json_post.user_name = post.getUser().getName();
    }

    return json_post;
  }

  private static 
  List<PostJson> PostModelToJson(Collection<PostModel> posts, 
                                 boolean include_user) {
    return posts.stream().map(p -> PostModelToJson(p, include_user))
           .collect(Collectors.toList());
  }

  @GetMapping("/get_posts")
  public List<PostJson> getPosts(@RequestParam int page,
                                 @RequestParam int size) {
    if (page < 0) {
      page = 0;
    }
    if (size < 0 || size > 100) {
      size = 100;
    }
    var page_req = PageRequest.of(page, size, Sort.Direction.DESC, 
                                  "post_date");
    var posts = post_repo.findPosts(page_req);
    boolean include_user = true;
    return PostModelToJson(posts, include_user);
  }

  @GetMapping("/get_posts_by_tags")
  public List<PostJson> getPostsByTags(@RequestParam List<String> tags,
                                       @RequestParam int page,
                                       @RequestParam int size) {
    if (page < 0) {
      page = 0;
    }
    if (size < 0 || size > 100) {
      size = 100;
    }
    var page_req = PageRequest.of(page, size, Sort.Direction.DESC, 
                                  "post_date");
    var posts = post_repo.findByTags(tags, page_req);
    boolean include_user = true;
    return PostModelToJson(posts, include_user);
  }

  @GetMapping("/get_user_posts")
  public List<PostJson> getUserPosts(@RequestParam String username,
                                     @RequestParam int page,
                                     @RequestParam int size) {
    if (page < 0) {
      page = 0;
    }
    if (size < 0 || size > 100) {
      size = 100;
    }
    var page_req = PageRequest.of(page, size, Sort.Direction.DESC, 
                                  "post_date");
    var posts = post_repo.findByUserName(username, page_req);
    boolean include_user = false;
    return PostModelToJson(posts, include_user);
  }

  @PostMapping("/create_user")
  public UserTokenJson createUser(@RequestParam String name, 
                                  @RequestParam String password,
                                  HttpServletResponse res) {
    var user_token = new UserTokenJson();
    if (disable_create_user) {
      res.setStatus(404);
      System.out.println("create_user disabled");
      return null;
    }

    var opt_user = user_repo.findByName(name);
    if (opt_user.isPresent()) {
      res.setStatus(403);
      user_token.msg = "User " + name + " is already created";
      return user_token;
    }

    var user = new UserModel();
    user.setName(name);
    user.setPassword(password); 
    user_repo.save(user);
    System.out.println("created user " + name);
    user_token = Security.genSession(secret_key, user.getId(), 
                                     session_valid_days);
    return user_token;
  }

  @GetMapping("/login")
  public UserTokenJson login(@RequestParam String name, 
                             @RequestParam String password,
                             HttpServletResponse res) {
    var user_token = new UserTokenJson();
    var opt_user = user_repo.findByName(name);
    UserModel user = opt_user.isPresent() ? opt_user.get() : null;
    if (user == null || !user.isPasswordCorrect(password)) {
      res.setStatus(403);
      user_token.msg = "Username or password is incorrect";
      return user_token;
    }

    System.out.println("User " + name + " logged in");
    user_token = Security.genSession(secret_key, user.getId(), 
                                     session_valid_days);
    return user_token;
  }

  @PostMapping("/delete_user")
  public MessageJson deleteUser(@RequestParam String name, 
                                @RequestParam String password,
                                HttpServletResponse res) {
    var msg = new MessageJson();
    var opt_user = user_repo.findByName(name);
    UserModel user = opt_user.isPresent() ? opt_user.get() : null;
    if (user == null || !user.isPasswordCorrect(password)) {
      res.setStatus(403);
      msg.msg = "Username or password is incorrect";
      return msg;
    }

    post_repo.deleteUserPosts(user.getId());
    user_repo.delete(user);
    msg.msg = "Success";
    return msg;
  }



  @PostMapping("/add_post")
  public PostJson addPost(@RequestParam String session, 
                          @RequestParam String title,
                          @RequestParam String text,
                          @RequestParam(name="tags") List<String> tag_names,
                          HttpServletResponse res) {
    UserTokenJson token = Security.checkSession(secret_key, session);
    UserModel user = null;
    if (token != null) {
      var opt_user = user_repo.findById(token.id);
      if (opt_user.isPresent()) {
        user = opt_user.get();
      }
    }

    if (token == null || user == null) {
      res.setStatus(403);
      var result = new PostJson();
      result.msg = "Error: invalid token";
      return result;
    }

    var tags = tag_repo.findByNames(tag_names);
    var tagset = tags.stream().map(t -> t.getName())
                 .collect(Collectors.toSet());

    for (var tagname : tag_names) {
      if (!tagset.contains(tagname)) {
        var tag = new TagModel();
        tag.setName(tagname);
        tag_repo.save(tag);
        tags.add(tag);
      }
    }

    var post = new PostModel();
    post.setTitle(title);
    post.setPostText(text);
    var cal = Calendar.getInstance();
    post.setPostDate(cal.getTime());
    post.setUser(user);
     
    for (var tag : tags) {
      post.addTag(tag);
    }
         
    post_repo.save(post);
    boolean include_user = false;
    return PostModelToJson(post, include_user);
  }

  @PostMapping("/delete_post")
  public MessageJson deletePost(@RequestParam String session, 
                                @RequestParam int id,
                                HttpServletResponse res) {
    var msg = new MessageJson();
    UserTokenJson token = Security.checkSession(secret_key, session);

    if (token == null) {
      res.setStatus(403);
      msg.msg = "Error: token";
      return msg;
    }

    var opt_post = post_repo.findByIdWithUser(id);
    if (!opt_post.isPresent()) {
      res.setStatus(403);
      msg.msg = "Error: post not found";
      return msg;
    }

    var post = opt_post.get();

    if (post.getUser().getId() != token.id) {
      res.setStatus(403);
      msg.msg = "Error: cannot delete post";
      return msg;
    }

    post_repo.delete(post);
    msg.msg = "Success";
    return msg;
  }
}
