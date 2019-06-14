package blog;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static blog.Data.assertUsersEqual;
import static blog.Data.assertPostsEqual;
import static blog.Data.assertTagGroupsEqualUnordered;
import static blog.Data.assertPostGroupsEqual;
import static blog.Data.assertPostGroupsEqualUnordered;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class ModelTest {
  @Autowired
  private UserRepo user_repo;
  @Autowired
  private PostRepo post_repo;
  @Autowired
  private TagRepo tag_repo;

  @Test
  public void userModelTest() throws Exception {
    var user = new UserModel();
    user.setName("foo");
    user.setPassword("1234");

    assertThat(user.getName()).isEqualTo("foo");
    assertThat(user.getPassword()).hasSize(64);
    assertThat(user.isPasswordCorrect("1234")).isEqualTo(true);
    assertThat(user.getSalt()).hasSize(16);
  }

  @Test
  public void userRepo_findByNameTest() throws Exception {
    var data = new Data(user_repo, post_repo, tag_repo);

    for (var user : data.all_users) {
      var user2 = user_repo.findByName(user.getName()).get();
      assertUsersEqual(user, user2);
    }

    var opt_user = user_repo.findByName("invalid_user");
    assertThat(opt_user).isEqualTo(Optional.empty());
  }

  @Test
  @Transactional
  public void deleteUserAndPostsTest() throws Exception {
    post_repo.deleteAll();
    tag_repo.deleteAll();
    user_repo.deleteAll();

    var num_posts = post_repo.count();
    var user = new UserModel("tmp_user_jdaar7", "1234");
    user_repo.save(user);
    int user_id = user.getId();

    var post = new PostModel("foo title", "foo text", user);
    var post2 = new PostModel("bar title", "bar text", user);
    post_repo.save(post);
    post_repo.save(post2);
    assertThat(post_repo.count()).isEqualTo(num_posts + 2);

    post_repo.deleteUserPosts(user_id);
    assertThat(post_repo.count()).isEqualTo(num_posts);
    var posts = post_repo.findByUserName(user.getName(), 
                                         PageRequest.of(0, 100));
    assertThat(posts).hasSize(0);

    user_repo.delete(user);
    var user_opt = user_repo.findById(user_id);
    assertThat(user_opt).isEqualTo(Optional.empty());
  }

  @Test
  public void postRepo_findPostsTest() throws Exception {
    var data = new Data(user_repo, post_repo, tag_repo);
    boolean include_user = true;
    int page_size = 2;

    for (int page = 0; page < 3; page++) {
      var page_req = PageRequest.of(page, page_size, Sort.Direction.DESC,
                                    "post_date");
      var posts = post_repo.findPosts(page_req);
      var expected_posts = data.getPosts(page, page_size);
      assertPostGroupsEqual(posts, expected_posts, include_user);
    }
  }

  @Test
  public void postRepo_findByTagsTest() throws Exception {
    var data = new Data(user_repo, post_repo, tag_repo);
    boolean include_user = true;

    var tag_groups = data.createTagCombos();
    int page_size = 2;

    for (var tag_group : tag_groups) {
      for (int page = 0; page < 2; page++) {
        var tag_names = tag_group.stream().map(t -> t.getName())
                        .collect(Collectors.toList());
        var page_req = PageRequest.of(page, page_size, Sort.Direction.DESC,
                                      "post_date");

        var posts = post_repo.findByTags(tag_names, page_req);
        var expected_posts = data.getPostsWithTags(tag_group, page, page_size);
        assertPostGroupsEqual(posts, expected_posts, include_user);
      }
    }

    var posts = post_repo.findByTags(List.of("bad_tag"), PageRequest.of(0, 5));
    assertThat(posts).hasSize(0);
  }

  @Test
  public void postRepo_findByIdWithUserTest() throws Exception {
    var data = new Data(user_repo, post_repo, tag_repo);
    boolean include_user = true;

    for (var post : data.all_posts) {
      var post2 = post_repo.findByIdWithUser(post.getId()).get();
      assertPostsEqual(post, post2, include_user);
    }

    var opt_post = post_repo.findByIdWithUser(-1234);
    assertThat(opt_post).isEqualTo(Optional.empty());
  }

  @Test
  public void postRepo_findByUserNameTest() throws Exception {
    var data = new Data(user_repo, post_repo, tag_repo);
    boolean include_user = true;
    int page_size = 2;

    for (var user : data.all_users) {
      for (int page = 0; page < 2; page++) {
        var page_req = PageRequest.of(page, page_size, Sort.Direction.DESC,
                                      "post_date");
        var posts = post_repo.findByUserName(user.getName(), page_req);
        var expected = data.getUserPosts(user.getId(), page, page_size);
        assertPostGroupsEqual(posts, expected, include_user);
      }
    }

    var posts = post_repo.findByUserName("invalid_user", PageRequest.of(0, 5));
    assertThat(posts).hasSize(0);
  }

  @Test
  public void tagRepo_findByNames() throws Exception {
    var data = new Data(user_repo, post_repo, tag_repo);

    var tag_groups = data.createTagCombos();

    for (var tag_group : tag_groups) {
      var tag_names = tag_group.stream().map(t -> t.getName())
                      .collect(Collectors.toList());
      var tags = tag_repo.findByNames(tag_names);
      assertTagGroupsEqualUnordered(tags, tag_group);
    }

    var tags = tag_repo.findByNames(List.of("bad_tag"));
    assertThat(tags).hasSize(0);
  }

}
