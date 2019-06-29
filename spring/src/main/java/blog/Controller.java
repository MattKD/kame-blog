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
import org.springframework.transaction.annotation.Transactional;


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
  @Value("${kame.debug_output}") 
  private boolean debug_output;

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
    if (debug_output) {
      System.out.println("/get_posts: page=" + page + ", size=" + size);
    }

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

    if (debug_output) {
      System.out.println("/get_posts_by_tags: page=" + page + ", size=" + 
                         size + ", tags=" + new ArrayList(tags));
    }

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

    if (debug_output) {
      System.out.println("/get_user_posts: username=" + username + ", page=" + 
                         page + ", size=" + size);
    }

    var user_opt = user_repo.findByName(username);
    if (!user_opt.isPresent()) {
      res.setStatus(403);
      var result = new PostListJson();
      result.msg = "User not found";
      if (debug_output) {
        System.out.println("/get_user_posts error(403): " + result.msg);
      }
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
  public UserTokenJson createUser(@RequestParam String username, 
                                  @RequestParam String password,
                                  HttpServletResponse res) {
    if (debug_output) {
      System.out.println("/create_user: username=" + username);
    }

    var name_regex = "^[a-zA-Z]+[a-zA-Z_]*$";
    var pw_regex = "^[-a-zA-Z0-9_!@#$%^&*+=)(]+$";
    var user_token = new UserTokenJson();

    if (username.length() < 2 || !username.matches(name_regex)) {
      res.setStatus(400);
      user_token.msg = "Name is too short or in a bad format";
      if (debug_output) {
        System.out.println("/create_user error(400): " + user_token.msg);
      }
      return user_token;
    }

    if (password.length() < 8 || !username.matches(pw_regex)) {
      res.setStatus(400);
      user_token.msg = "Password is too short or in a bad format";
      if (debug_output) {
        System.out.println("/create_user error(400): " + user_token.msg);
      }
      return user_token;
    }

    if (disable_create_user) {
      res.setStatus(404);
      user_token.msg = "Account creation is disabled";
      if (debug_output) {
        System.out.println("/create_user error(404): " + user_token.msg);
      }
      return user_token;
    }

    var opt_user = user_repo.findByName(username);
    if (opt_user.isPresent()) {
      res.setStatus(403);
      user_token.msg = "User " + username + " is in use";
      if (debug_output) {
        System.out.println("/create_user error(403): " + user_token.msg);
      }
      return user_token;
    }

    var user = new UserModel();
    user.setName(username);
    user.setPassword(password); 
    user_repo.save(user);
    user_token = Security.genSession(secret_key, user.getId(), 
                                     session_valid_days);
    return user_token;
  }

  @GetMapping("/login")
  public UserTokenJson login(@RequestParam String username, 
                             @RequestParam String password,
                             HttpServletResponse res) {

    if (debug_output) {
      System.out.println("/login: username=" + username);
    }

    var user_token = new UserTokenJson();
    var opt_user = user_repo.findByName(username);
    UserModel user = opt_user.isPresent() ? opt_user.get() : null;
    if (user == null || !user.isPasswordCorrect(password)) {
      res.setStatus(403);
      user_token.msg = "Username or password is incorrect";
      if (debug_output) {
        System.out.println("/login error(403): " + user_token.msg);
      }
      return user_token;
    }

    user_token = Security.genSession(secret_key, user.getId(), 
                                     session_valid_days);
    return user_token;
  }

  @Transactional
  @PostMapping("/delete_user")
  public MessageJson deleteUser(@RequestParam String username, 
                                @RequestParam String password,
                                HttpServletResponse res) {

    if (debug_output) {
      System.out.println("/delete_user: username=" + username);
    }

    var msg = new MessageJson();
    var opt_user = user_repo.findByName(username);
    UserModel user = opt_user.isPresent() ? opt_user.get() : null;
    if (user == null || !user.isPasswordCorrect(password)) {
      res.setStatus(403);
      msg.msg = "Username or password is incorrect";
      if (debug_output) {
        System.out.println("/delete_user error(403): " + msg.msg);
      }
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

    if (debug_output) {
      var tags = new ArrayList<String>();
      if (tag_names != null) {
        tags.addAll(tag_names);
      }
                               
      System.out.println("/add_post: title=" + title + ", text=" + text +
                         ", tags=" + tags);
    }

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
      result.msg = "Invalid token";
      if (debug_output) {
        System.out.println("/add_post error(403): " + result.msg);
      }
      return result;
    }

    title = title.trim();
    text = text.trim();
    var bad_input = false;
    var msg = "";

    if (tag_names != null) {
      tag_names = tag_names.stream().map(String::trim)
                  .collect(Collectors.toList());

      for (var tag : tag_names) {
        if (!tag.matches("^[a-zA-Z]+[a-zA-Z0-9_]*$") || tag.length() > 16) {
          msg = "Tags are too long or a bad format";
          bad_input = true;
        }
      }
    }

    if (title.equals("") || text.equals("")) {
      msg = "Title and Text can't be empty";
      bad_input = true;
    }

    if (bad_input) {
      res.setStatus(400);
      var result = new PostJson();
      result.msg = msg;
      if (debug_output) {
        System.out.println("/add_post error(400): " + result.msg);
      }
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
    if (debug_output) {
      System.out.println("/delete_post: id=" + id);
    }

    var msg = new MessageJson();
    UserTokenJson token = Security.checkSession(secret_key, session);

    if (token == null) {
      res.setStatus(403);
      msg.msg = "Invalid token";
      if (debug_output) {
        System.out.println("/delete_post error(403): " + msg.msg);
      }
      return msg;
    }

    var opt_post = post_repo.findByIdWithUser(id);
    if (!opt_post.isPresent()) {
      res.setStatus(403);
      msg.msg = "Post not found";
      if (debug_output) {
        System.out.println("/delete_post error(403): " + msg.msg);
      }
      return msg;
    }

    var post = opt_post.get();

    if (post.getUser().getId() != token.id) {
      res.setStatus(403);
      msg.msg = "Cannot delete post";
      if (debug_output) {
        System.out.println("/delete_post error(403): " + msg.msg);
      }
      return msg;
    }

    post_repo.delete(post);
    msg.msg = "Success";
    return msg;
  }
}
