package blog;

import java.util.Optional;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

//import static blog.Data.assertUserJsonsEqualPostModels;
import static blog.Data.assertPostJsonsEqualPostModels;
import static blog.Data.assertDatesEqualNoMS;

@RunWith(SpringRunner.class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.MOCK,
  classes = App.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test.properties")
public class ControllerTest {
  @Autowired
  private UserRepo user_repo;
  @Autowired
  private PostRepo post_repo;
  @Autowired
  private TagRepo tag_repo;

  // defined in test.properties
  @Value("${kame.secret_key}") 
  private String secret_key;
  @Value("${kame.session_valid_days}") 
  private int session_valid_days;

  @Autowired
  private MockMvc mvc;

  @Test
  public void getPostsTest() throws Exception {
    // initialize db to test defaults
    var data = new Data(user_repo, post_repo, tag_repo);

    // test getting posts with pagination

    Integer page_size = 2;

    for (int page = 0; page < 3; page++) {
      var req = MockMvcRequestBuilders.get("/get_posts")
                .accept(MediaType.APPLICATION_JSON);
      req.param("page", new Integer(page).toString());
      req.param("size", page_size.toString());

      var res = mvc.perform(req).andReturn().getResponse();

      assertThat(res.getStatus()).isEqualTo(200);
      assertThat(res.getContentType()).startsWith("application/json");

      PostListJson posts = new ObjectMapper()
        .readValue(res.getContentAsString(), PostListJson.class);

      var expected = data.getPosts(page, page_size);
      boolean include_user = true;
      assertPostJsonsEqualPostModels(posts.posts, expected, include_user);
    }

    // test getting posts out of range

    var req = MockMvcRequestBuilders.get("/get_posts")
              .accept(MediaType.APPLICATION_JSON);
    req.param("page", "100");
    req.param("size", "100");

    var res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(200);
    assertThat(res.getContentType()).startsWith("application/json");

    PostListJson posts = new ObjectMapper()
      .readValue(res.getContentAsString(), PostListJson.class);
   
    assertThat(posts.posts).hasSize(0);
  }

  @Test
  public void getPostsByTagsTest() throws Exception {
    // initialize db to test defaults
    var data = new Data(user_repo, post_repo, tag_repo);

    // test getting posts for each tag and combos of 2 tags

    var tag_groups = data.createTagCombos();
    Integer page_size = 2;

    for (var tag_group : tag_groups) {
      for (int page = 0; page < 2; page++) {
        var tag_names = tag_group.stream().map(t -> t.getName())
                        .collect(Collectors.toList());

        var req = MockMvcRequestBuilders.get("/get_posts_by_tags")
                  .accept(MediaType.APPLICATION_JSON);
        for (var tag : tag_group) {
          req.param("tags", tag.getName());
        }
        req.param("page", new Integer(page).toString());
        req.param("size", page_size.toString());

        var res = mvc.perform(req).andReturn().getResponse();

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(res.getContentType()).startsWith("application/json");

        PostListJson posts = new ObjectMapper()
          .readValue(res.getContentAsString(), PostListJson.class);
       
        var expected = data.getPostsWithTags(tag_group, page, page_size);
        boolean include_user = true;
        assertPostJsonsEqualPostModels(posts.posts, expected, include_user);
      }
    }

    // test a tag not in database
    
    var req = MockMvcRequestBuilders.get("/get_posts_by_tags")
              .accept(MediaType.APPLICATION_JSON);
    req.param("tags", "bad_tag");
    req.param("page", "0");
    req.param("size", "5");
    var res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(200);
    assertThat(res.getContentType()).startsWith("application/json");

    PostListJson posts = new ObjectMapper()
      .readValue(res.getContentAsString(), PostListJson.class);
   
    assertThat(posts.posts).hasSize(0);
  }

  @Test
  public void getUserPostsTest() throws Exception {
    // initialize db to test defaults
    var data = new Data(user_repo, post_repo, tag_repo);

    // test getting posts from each user

    Integer page_size = 2;

    for (var user : data.all_users) {
      for (int page = 0; page < 2; page++) {
        var req = MockMvcRequestBuilders.get("/get_user_posts")
                  .accept(MediaType.APPLICATION_JSON)
                  .param("username", user.getName())
                  .param("page", new Integer(page).toString())
                  .param("size", page_size.toString());

        var res = mvc.perform(req).andReturn().getResponse();

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(res.getContentType()).startsWith("application/json");

        PostListJson posts = new ObjectMapper()
          .readValue(res.getContentAsString(), PostListJson.class);

        var expected = data.getUserPosts(user.getId(), page, page_size);
        boolean include_user = false;
        assertPostJsonsEqualPostModels(posts.posts, expected, include_user);
      }
    }

    // test a user not in database
    
    var req = MockMvcRequestBuilders.get("/get_user_posts")
              .accept(MediaType.APPLICATION_JSON)
              .param("username", "bad_user")
              .param("page", "0")
              .param("size", "5");

    var res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(403);
    assertThat(res.getContentType()).startsWith("application/json");

    PostListJson posts = new ObjectMapper()
      .readValue(res.getContentAsString(), PostListJson.class);

    assertThat(posts.msg).isNotNull();
    assertThat(posts.msg).hasSizeGreaterThan(0);
  }

  @Test
  public void createUserTest() throws Exception {
    // initialize db to test defaults
    var data = new Data(user_repo, post_repo, tag_repo);

    // create tmp user

    var username = "tmp_user";
    var password = "123456";
    var req = MockMvcRequestBuilders.post("/create_user")
              .accept(MediaType.APPLICATION_JSON)
              .param("name", username)
              .param("password", password);

    var res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(200);
    assertThat(res.getContentType()).startsWith("application/json");

    UserTokenJson token = new ObjectMapper()
        .readValue(res.getContentAsString(), UserTokenJson.class);
     
    var user = user_repo.findById(token.id).get();
    assertThat(token.id).isEqualTo(user.getId());
    assertThat(token.expires).isAfter(Calendar.getInstance().getTime());
    assertThat(token.token).hasSizeGreaterThan(15);

    // try recreating user

    req = MockMvcRequestBuilders.post("/create_user")
              .accept(MediaType.APPLICATION_JSON)
              .param("name", username)
              .param("password", password);

    res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(403);
    assertThat(res.getContentType()).startsWith("application/json");

    token = new ObjectMapper()
        .readValue(res.getContentAsString(), UserTokenJson.class);
    assertThat(token.id).isNull();
    assertThat(token.expires).isNull();
    assertThat(token.token).isNull();
    assertThat(token.msg).isNotNull();
  }

  @Test
  @Transactional
  public void deleteUserTest() throws Exception {
    post_repo.deleteAll();
    tag_repo.deleteAll();
    user_repo.deleteAll();

    var num_posts = post_repo.count();
    var num_users = user_repo.count();

    // create tmp user and posts

    var username = "tmp_user_aj34";
    var password = "pass12";
    var user = new UserModel(username, password);
    user_repo.save(user);
    assertThat(user_repo.count()).isEqualTo(num_users + 1);

    var post = new PostModel("foo title", "foo text", user);
    var post2 = new PostModel("bar title", "bar text", user);
    post_repo.save(post);
    post_repo.save(post2);
    assertThat(post_repo.count()).isEqualTo(num_posts + 2);

    // test deleting with wrong password

    var req = MockMvcRequestBuilders.post("/delete_user")
            .accept(MediaType.APPLICATION_JSON)
            .param("name", username)
            .param("password", password + "!");

    var res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(403);
    assertThat(res.getContentType()).startsWith("application/json");

    MessageJson msg = new ObjectMapper()
        .readValue(res.getContentAsString(), MessageJson.class);
    assertThat(msg.msg).isNotNull();
    assertThat(msg.msg).isNotEqualTo("Success");
    assertThat(post_repo.count()).isEqualTo(num_posts + 2);
    assertThat(user_repo.count()).isEqualTo(num_users + 1);
 
    // delete user and posts tmp user

    req = MockMvcRequestBuilders.post("/delete_user")
              .accept(MediaType.APPLICATION_JSON)
              .param("name", username)
              .param("password", password);

    res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(200);
    assertThat(res.getContentType()).startsWith("application/json");

    msg = new ObjectMapper()
        .readValue(res.getContentAsString(), MessageJson.class);
     
    assertThat(msg.msg).isEqualTo("Success");
    assertThat(post_repo.count()).isEqualTo(num_posts);
    assertThat(user_repo.count()).isEqualTo(num_users);
  }


  @Test
  public void loginTest() throws Exception {
    // initialize db to test defaults
    var data = new Data(user_repo, post_repo, tag_repo);

    // create tmp user

    var username = "tmp_user";
    var password = "pass12";
    var user = new UserModel(username, password);
    user_repo.save(user);

    // login to tmp user

    var req = MockMvcRequestBuilders.get("/login")
              .accept(MediaType.APPLICATION_JSON)
              .param("name", username)
              .param("password", password);

    var res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(200);
    assertThat(res.getContentType()).startsWith("application/json");

    UserTokenJson token = new ObjectMapper()
        .readValue(res.getContentAsString(), UserTokenJson.class);
     
    assertThat(token.id).isEqualTo(user.getId());
    assertThat(token.expires).isAfter(Calendar.getInstance().getTime());
    assertThat(token.token).hasSizeGreaterThan(15);

    // test logging with wrong password

    req = MockMvcRequestBuilders.get("/login")
            .accept(MediaType.APPLICATION_JSON)
            .param("name", username)
            .param("password", password + "!");

    res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(403);
    assertThat(res.getContentType()).startsWith("application/json");

    token = new ObjectMapper()
        .readValue(res.getContentAsString(), UserTokenJson.class);
     
    assertThat(token.id).isNull();
    assertThat(token.expires).isNull();
    assertThat(token.token).isNull();
    assertThat(token.msg).isNotNull();
  }

  @Test
  public void addPostTest() throws Exception {
    // initialize db to test defaults
    var data = new Data(user_repo, post_repo, tag_repo);

    // create tmp user

    var username = "tmp_user";
    var password = "pass12";
    var user = new UserModel(username, password);
    user_repo.save(user);

    var token = Security.genSession(secret_key, user.getId(), 
                                    session_valid_days);

    // create post for tmp user

    List<List<String>> tags = List.of(List.of(), List.of("hello"), 
                                      List.of("foo", "bar"));
    for (int i = 0; i < tags.size(); i++) {
      var title = "Hello World!" + i;
      var text = "Hello there!" + i;
      var req = MockMvcRequestBuilders.post("/add_post")
                .accept(MediaType.APPLICATION_JSON)
                .param("session", token.token)
                .param("title", title)
                .param("text", text);
      for (var tag : tags.get(i)) {
        req.param("tags", tag);
      }

      var res = mvc.perform(req).andReturn().getResponse();

      assertThat(res.getStatus()).isEqualTo(200);
      assertThat(res.getContentType()).startsWith("application/json");

      PostJson post = new ObjectMapper()
          .readValue(res.getContentAsString(), PostJson.class);
       
      assertThat(post.id).isNotNull();
      var expected = post_repo.findById(post.id).get();

      assertThat(post.id).isEqualTo(expected.getId());
      assertThat(post.title).isEqualTo(title);
      assertThat(post.title).isEqualTo(expected.getTitle());
      assertThat(post.text).isEqualTo(text);
      assertThat(post.text).isEqualTo(expected.getPostText());
      assertDatesEqualNoMS(post.date, expected.getPostDate());

      post_repo.delete(expected);
    }

    // test creating post with bad session token

    var req = MockMvcRequestBuilders.post("/add_post")
            .accept(MediaType.APPLICATION_JSON)
            .param("session", "bad_token")
            .param("title", "foo title")
            .param("text", "bar text");

    var res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(403);
    assertThat(res.getContentType()).startsWith("application/json");

    var post = new ObjectMapper()
        .readValue(res.getContentAsString(), PostJson.class);
     
    assertThat(post.id).isNull();
    assertThat(post.title).isNull();
    assertThat(post.text).isNull();
    assertThat(post.date).isNull();
    assertThat(post.msg).isNotNull();
  }

  @Test
  public void deletePostTest() throws Exception {
    // initialize db to test defaults
    var data = new Data(user_repo, post_repo, tag_repo);

    // create tmp user

    var username = "tmp_user";
    var password = "pass12";
    var user = new UserModel(username, password);
    user_repo.save(user);

    var token = Security.genSession(secret_key, user.getId(), 
                                    session_valid_days);

    // create tmp post
    var tags = List.of(new TagModel("tmp_tag"));
    tag_repo.saveAll(tags);
    var post = new PostModel("title", "text", user, tags);
    post_repo.save(post);

    // delete tmp post
    
    var req = MockMvcRequestBuilders.post("/delete_post")
              .accept(MediaType.APPLICATION_JSON)
              .param("session", token.token)
              .param("id", post.getId().toString());

    var res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(200);
    assertThat(res.getContentType()).startsWith("application/json");

    MessageJson msg = new ObjectMapper()
        .readValue(res.getContentAsString(), MessageJson.class);
    assertThat(msg.msg).isEqualTo("Success");
    var opt_post = post_repo.findById(post.getId());
    assertThat(opt_post).isEqualTo(Optional.empty());

    // try deleting post with bad token

    // add post again
    post = new PostModel("title", "text", user, tags);
    post_repo.save(post);

    req = MockMvcRequestBuilders.post("/delete_post")
              .accept(MediaType.APPLICATION_JSON)
              .param("session", "bad_token")
              .param("id", post.getId().toString());

    res = mvc.perform(req).andReturn().getResponse();

    assertThat(res.getStatus()).isEqualTo(403);
    assertThat(res.getContentType()).startsWith("application/json");

    msg = new ObjectMapper()
        .readValue(res.getContentAsString(), MessageJson.class);
    assertThat(msg.msg).isNotNull();
    assertThat(msg.msg).isNotEqualTo("Success");
    opt_post = post_repo.findById(post.getId());
    assertThat(opt_post).isNotEqualTo(Optional.empty());
  }
}
