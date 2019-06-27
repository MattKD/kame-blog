package blog;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
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
  private boolean disable_create_user;
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
  PostListJson PostModelToJson(Collection<PostModel> posts, 
                               boolean include_user) {
    var posts_json = posts.stream().map(p -> PostModelToJson(p, include_user))
                     .collect(Collectors.toList());
    var post_list = new PostListJson();
    post_list.posts = posts_json;
    return post_list;
  }

  @GetMapping("/get_posts")
  public PostListJson getPosts(@RequestParam(defaultValue="0") int page,
                               @RequestParam(defaultValue="100") int size) {
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
  public PostListJson getPostsByTags(
      @RequestParam List<String> tags,
      @RequestParam(defaultValue="0") int page,
      @RequestParam(defaultValue="100") int size) {

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
  public PostListJson getUserPosts(
      @RequestParam String username,
      @RequestParam(defaultValue="0") int page,
      @RequestParam(defaultValue="100") int size,
      HttpServletResponse res) {

    var user_opt = user_repo.findByName(username);
    if (!user_opt.isPresent()) {
      res.setStatus(403);
      var result = new PostListJson();
      result.msg = "Error: User not found";
      return result;
    }

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

  /*
   * name must be at least 2 chars; start with a-z or A-Z; and contain only
   * a-z, A-Z, or an underscore.
   * password must be at least 8 chars, and contain non-whitespace ascii
   */
  @PostMapping("/create_user")
  public UserTokenJson createUser(@RequestParam String name, 
                                  @RequestParam String password,
                                  HttpServletResponse res) {
    var name_regex = "^[a-zA-Z]+[a-zA-Z_]*$";
    var pw_regex = "^[-a-zA-Z0-9_!@#$%^&*+=)(]+$";
    var user_token = new UserTokenJson();
    if (name.length() < 2 || !name.matches(name_regex)) {
      res.setStatus(400);
      user_token.msg = "Error: name is too short or in bad format";
      return user_token;
    }
    if (password.length() < 8 || !name.matches(pw_regex)) {
      res.setStatus(400);
      user_token.msg = "Error: password must be at least 8 chars";
      return user_token;
    }

    if (disable_create_user) {
      res.setStatus(404);
      System.out.println("create_user disabled");
      user_token.msg = "Error: Account creating is disabled";
      return user_token;
    }

    var opt_user = user_repo.findByName(name);
    if (opt_user.isPresent()) {
      res.setStatus(403);
      user_token.msg = "Error: User " + name + " is in use";
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
    name = name.trim();
    password = password.trim();
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
    name = name.trim();
    password = password.trim();
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

  /*
   * title must be at least 1 char after whitespace trim.
   * text must be at least 1 char after whitespace trim.
   * tags must be between 1 to 16 chars after whitespace trim; start letter 
   * a-z or A-Z; contain only a-z, A-Z, 0-9, or an underscore.
   */
  @PostMapping("/add_post")
  public PostJson addPost(
      @RequestParam String session, 
      @RequestParam String title,
      @RequestParam String text,
      @RequestParam(name="tags", required=false) List<String> tag_names,
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

    boolean bad_input = false;
    title = title.trim();
    text = text.trim();

    if (tag_names != null) {
      tag_names = tag_names.stream().map(String::trim)
                  .collect(Collectors.toList());

      for (var tag : tag_names) {
        if (!tag.matches("^[a-zA-Z]+[a-zA-Z0-9_]*$") || tag.length() > 16) {
          bad_input = true;
        }
      }
    }

    if (title.equals("") || text.equals("")) {
      bad_input = true;
    }

    if (bad_input) {
      res.setStatus(400);
      var result = new PostJson();
      result.msg = "Error: bad input";
      return result;
    }

    List<TagModel> tags = new ArrayList<>();
    if (tag_names != null) {
      tags = tag_repo.findByNames(tag_names);
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
    }

    var post = new PostModel();
    post.setTitle(title);
    post.setPostText(text);
    var cal = Calendar.getInstance();
    post.setPostDate(cal.getTime());
    post.setUser(user);
    post.addTags(tags);
         
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
