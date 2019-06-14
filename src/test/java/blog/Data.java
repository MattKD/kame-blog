package blog;

import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class Data {
  public List<UserModel> all_users = new ArrayList<>();
  public List<TagModel> all_tags = new ArrayList<>();
  public List<PostModel> all_posts = new ArrayList<>();
  public Map<Integer, List<PostModel>> user_posts = new HashMap<>();
  public Map<Integer, List<PostModel>> tag_posts = new HashMap<>();

  public Data(UserRepo user_repo, PostRepo post_repo, TagRepo tag_repo) {
    post_repo.deleteAll();
    tag_repo.deleteAll();
    user_repo.deleteAll();

    // create tags

    var tag_fun = new TagModel("fun");
    var tag_highground = new TagModel("high-ground");
    var tag_hello = new TagModel("hello");
    var tag_badfeeling = new TagModel("bad-feeling");
    var tag_power = new TagModel("power");

    all_tags.add(tag_fun);
    all_tags.add(tag_highground);
    all_tags.add(tag_hello);
    all_tags.add(tag_badfeeling);
    all_tags.add(tag_power);

    tag_repo.save(tag_fun);
    tag_repo.save(tag_highground);
    tag_repo.save(tag_hello);
    tag_repo.save(tag_badfeeling);
    tag_repo.save(tag_power);

    // create users

    var user_anakin = new UserModel("anakin", "1234");
    var user_obi = new UserModel("obi", "1234");
    var user_palpatine = new UserModel("palpatine", "1234");

    all_users.add(user_anakin);
    all_users.add(user_obi);
    all_users.add(user_palpatine);

    user_repo.save(user_anakin);
    user_repo.save(user_obi);
    user_repo.save(user_palpatine);

    // create posts

    var posts_anakin = List.of(
        new PostModel("fun", "This is where the fun begins", user_anakin, 
                      List.of(tag_fun)),
        new PostModel("bad feeling", "I've got a bad feeling about this", 
                      user_anakin, List.of(tag_badfeeling)),
        new PostModel("power", "You underestimate my power", user_anakin,
                      List.of(tag_power)));

    var posts_obi = List.of(
        new PostModel("Grievous", "Hello there", user_obi,
                     List.of(tag_hello, tag_fun)),
        new PostModel("bad feeling", "Oh, I have a bad feeling about this", 
                      user_obi, List.of(tag_badfeeling)),
        new PostModel("It's over", "I have the high ground", user_obi, 
                      List.of(tag_highground)));

     var posts_palpatine = List.of(
        new PostModel("Senate", "I am the Senate!", user_palpatine,
                      List.of(tag_power)),
        new PostModel("Power!", "Unlimited power!", user_palpatine, 
                      List.of(tag_power, tag_fun)),
        new PostModel("Darth Plagueis", 
                      "You ever hear the tragedy of Darth Plagueis the Wise?", 
                      user_palpatine, List.of(tag_fun)));

    all_posts.addAll(posts_anakin);
    all_posts.addAll(posts_obi);
    all_posts.addAll(posts_palpatine);

    int day = 1;
    for (var post : all_posts) {
      post.setPostDate(createDate(2019, 1, day++));
    }

    user_posts.put(user_anakin.getId(), posts_anakin);
    user_posts.put(user_obi.getId(), posts_obi);
    user_posts.put(user_palpatine.getId(), posts_palpatine);

    for (var post : all_posts) {
      for (var tag : post.getTags()) {
        var t_posts = tag_posts.get(tag.getId());
        if (t_posts == null) {
          t_posts = new ArrayList<PostModel>();
          tag_posts.put(tag.getId(), t_posts);
        }
        t_posts.add(post);
      }
    }

    post_repo.saveAll(posts_anakin);
    post_repo.saveAll(posts_obi);
    post_repo.saveAll(posts_palpatine);
  }

  private static Date createDate(int year, int month, int day) {
    var cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DAY_OF_MONTH, day);
    return cal.getTime();
  }

  public static void assertDatesEqualNoMS(Date d, Date d2) {
    var cal = Calendar.getInstance();
    cal.setTime(d);
    cal.set(Calendar.MILLISECOND, 0);

    var cal2 = Calendar.getInstance();
    cal2.setTime(d2);
    cal2.set(Calendar.MILLISECOND, 0);

    assertThat(cal.getTime()).isEqualTo(cal2.getTime());
  }

  public static void assertUsersEqual(UserModel u, UserModel u2) {
    assertThat(u).isNotNull();
    assertThat(u2).isNotNull();
    assertThat(u.getId()).isEqualTo(u2.getId());
    assertThat(u.getName()).isEqualTo(u2.getName());
    assertThat(u.getPassword()).isEqualTo(u2.getPassword());
  }

  public static void assertPostsEqual(PostModel p, PostModel p2,
                                      boolean include_user) {
    assertThat(p).isNotNull();
    assertThat(p2).isNotNull();
    assertThat(p.getId()).isEqualTo(p2.getId());
    assertThat(p.getTitle()).isEqualTo(p2.getTitle());
    assertThat(p.getPostText()).isEqualTo(p2.getPostText());
    assertTagGroupsEqualUnordered(p.getTags(), p2.getTags());

    if (include_user) {
      assertThat(p2.getUser().getId()).isEqualTo(p.getUser().getId());
      assertThat(p2.getUser().getName()).isEqualTo(p.getUser().getName());
    }
  }

  public static void assertPostsEqual(PostJson p, PostModel p2, 
                                      boolean include_user) {
    assertThat(p).isNotNull();
    assertThat(p2).isNotNull();
    assertThat(p2.getId()).isEqualTo(p.id);
    assertThat(p2.getTitle()).isEqualTo(p.title);
    assertThat(p2.getPostText()).isEqualTo(p.text);
    var expected_tags = p2.getTags().stream().map(TagModel::getName)
                        .collect(Collectors.toList());
    assertThat(p.tags).containsExactlyElementsOf(expected_tags);

    if (include_user) {
      assertThat(p2.getUser().getId()).isEqualTo(p.user_id);
      assertThat(p2.getUser().getName()).isEqualTo(p.user_name);
    }
  }

  public static void assertTagGroupsEqualUnordered(
      Iterable<TagModel> tags, Iterable<TagModel> expected) {

    assertThat(tags).isNotNull();
    assertThat(expected).isNotNull();
    assertThat(tags).hasSameSizeAs(expected);

    var tag_map = new HashMap<Integer, String>();
    for (var tag : tags) {
      tag_map.put(tag.getId(), tag.getName());
    }

    for (var tag : expected) {
      assertThat(tag_map).containsKey(tag.getId());
      assertThat(tag_map.get(tag.getId())).isEqualTo(tag.getName());
    }
  }

  public static 
  void assertPostGroupsEqualUnordered(Iterable<PostModel> posts, 
                                      Iterable<PostModel> expected,
                                      boolean include_user) {

    assertThat(posts).isNotNull();
    assertThat(expected).isNotNull();
    assertThat(posts).hasSameSizeAs(expected);

    var post_map = new HashMap<Integer, PostModel>();
    for (var post : posts) {
      post_map.put(post.getId(), post);
    }

    for (var post : expected) {
      assertThat(post_map).containsKey(post.getId());
      var post2 = post_map.get(post.getId());
      assertPostsEqual(post, post2, include_user);
    }
  }

  public static 
  void assertPostGroupsEqual(List<PostModel> posts, List<PostModel> expected,
                             boolean include_user) {
    assertThat(posts).isNotNull();
    assertThat(expected).isNotNull();
    assertThat(posts).hasSameSizeAs(expected);

    for (int i = 0; i < posts.size(); i++) {
      assertPostsEqual(posts.get(i), expected.get(i), include_user);
    }
  }

  public static 
  void assertPostJsonsEqualPostModelsUnordered(Iterable<PostJson> posts, 
                                               Iterable<PostModel> expected,
                                               boolean include_user) {
    assertThat(posts).isNotNull();
    assertThat(expected).isNotNull();
    assertThat(posts).hasSameSizeAs(expected);

    var post_map = new HashMap<Integer, PostJson>();
    for (var post : posts) {
      post_map.put(post.id, post);
    }

    for (var post : expected) {
      assertThat(post_map).containsKey(post.getId());
      var post2 = post_map.get(post.getId());
      assertPostsEqual(post2, post, include_user);
    }
  }

  public static 
  void assertPostJsonsEqualPostModels(List<PostJson> posts, 
                                      List<PostModel> expected,
                                      boolean include_user) {
    assertThat(posts).isNotNull();
    assertThat(expected).isNotNull();
    assertThat(posts).hasSameSizeAs(expected);

    for (int i = 0; i < posts.size(); i++) {
      assertPostsEqual(posts.get(i), expected.get(i), include_user);
    }
  }

  /*
   * create all tag combos of size 1 and 2
   */
  public List<List<TagModel>> createTagCombos() {
    var tag_groups = new ArrayList<List<TagModel>>();
    for (int i = 0; i < all_tags.size(); i++) {
      tag_groups.add(List.of(all_tags.get(i)));

      for (int j = i + 1; j < all_tags.size(); j++) {
        tag_groups.add(List.of(all_tags.get(i), all_tags.get(j)));
      }
    }

    return tag_groups;
  }

  /*
   * get expected posts with tags, pagination, and sorted by newest
   */
  public List getPostsWithTags(Iterable<TagModel> tags, int page, int size) {
    var post_set = new HashSet<PostModel>();
    for (var tag : tags) {
      var posts = tag_posts.get(tag.getId());  
      post_set.addAll(posts);
    }

    var expected = new ArrayList<PostModel>(post_set);
    expected.sort((p1, p2) -> p2.getPostDate().compareTo(p1.getPostDate()));

    int start = page * size;
    if (start >= expected.size()) {
      return new ArrayList<PostModel>();
    }
    int end = start + size;
    if (end > expected.size()) {
      end = expected.size();
    }

    return expected.subList(start, end);
  }

  /*
   * get expected user posts with pagination, and sorted by newest
   */
  public List getUserPosts(int id, int page, int size) {
    var expected = new ArrayList<PostModel>(user_posts.get(id));
    expected.sort((p1, p2) -> p2.getPostDate().compareTo(p1.getPostDate()));

    int start = page * size;
    if (start >= expected.size()) {
      return new ArrayList<PostModel>();
    }
    int end = start + size;
    if (end > expected.size()) {
      end = expected.size();
    }

    return expected.subList(start, end);
  }

  /*
   * get expected posts with pagination, and sorted by newest
   */
  public List getPosts(int page, int size) {
    var expected = new ArrayList<PostModel>(all_posts);
    expected.sort((p1, p2) -> p2.getPostDate().compareTo(p1.getPostDate()));

    int start = page * size;
    if (start >= expected.size()) {
      return new ArrayList<PostModel>();
    }
    int end = start + size;
    if (end > expected.size()) {
      end = expected.size();
    }

    return expected.subList(start, end);
  }



}
