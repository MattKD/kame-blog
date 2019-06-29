const React = require("react");
const { Link } = require("react-router-dom");
const PostList = require("./PostList");
const PageHeader = require("./PageHeader");
const AddPostForm = require("./AddPostForm");
const DeletePostForm = require("./DeletePostForm");
const DeleteUserForm = require("./DeleteUserForm");
const ErrorPage = require("./ErrorPage");
const { getUserPosts } = require("./api");
const { posts_per_page } = require("./config");

// props:
//   user_posts
//   session 
//   updateUserPosts(user_posts)
//   onAddPost(post_res)
//   onDeletePost(post_id)
//   onDeleteUser(userid, name)
class UserPage extends React.PureComponent {
  constructor(props) {
    super(props);
    this.getUserPosts = this.getUserPosts.bind(this);
  }

  getUserPosts(username, page) {
    const user_posts = this.props.user_posts;
    const updateUserPosts = this.props.updateUserPosts;
    
    getUserPosts(username, page, posts_per_page).then((res) => {
      if (res.status === 200) {
        if (user_posts[username] === undefined) {
          user_posts[username] = {};
        }
        user_posts[username][page] = res.posts;
      } else {
        user_posts[username] = null;
      }
      updateUserPosts(user_posts);
    });
  }

  render() {
    const user_posts = this.props.user_posts;
    const params = this.props.match.params;
    const username = params.username;
    const page = params.page ? parseInt(params.page) : 0;
    const getUserPosts = this.getUserPosts;
    const session = this.props.session;
    const logged_in = session.loggedIn();
    const is_current_user = logged_in && session.name === username;
    const onAddPost = this.props.onAddPost;
    const onDeletePost = this.props.onDeletePost;
    const onDeleteUser = this.props.onDeleteUser;

    if (Number.isNaN(page)) {
      return <ErrorPage session={session} />;
    }

    document.title = username + "'s Posts";

    if (user_posts[username] === undefined || 
        user_posts[username][page] === undefined) {
      getUserPosts(username, page);
      return null;
    }

    if (user_posts[username] === null) {
      return <ErrorPage session={session} />;
    }

    const posts = user_posts[username][page];
    let header = username + " has no posts to show";
    if (posts.length > 0) {
      const start = page * posts_per_page + 1;
      const end = start + posts.length - 1;
      header = username + "'s Posts (" + start + " to " + end + ")";
    }

    const add_post_form = is_current_user ? (
      <div>
        <h3>Add Post</h3>
        <AddPostForm session={session} onAddPost={onAddPost} />
      </div>
    ) : null;

    const delete_user_form = is_current_user ? (
      <div>
        <h3>Delete Account</h3>
        <DeleteUserForm session={session} onDeleteUser={onDeleteUser}/>
      </div>
    ) : null;

    const post_list = is_current_user ?
      <PostList posts={posts} session={session} onDeletePost={onDeletePost}/> :
      <PostList posts={posts} />

    const has_next_page = posts.length === posts_per_page ? true : false;
    const next_page_link = has_next_page 
          ? <Link to={`/users/${username}/${page+1}`}
                  className="foot_link">Next Page</Link> 
          : undefined;
    const prev_page_link = page > 0 ?
          <Link to={`/users/${username}/${page-1}`} 
                className="foot_link">Prev Page</Link> : 
          undefined;

    return (
      <div>
        <PageHeader session={session}/>
        {delete_user_form}
        {add_post_form}
        <h2>{header}</h2>
        {post_list} 
        {prev_page_link}
        {next_page_link}
      </div>
    );
  }
}

module.exports = UserPage;
