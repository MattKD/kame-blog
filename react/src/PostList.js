const React = require("react");
const Post = require("./Post");

// props:
//   posts: [{id, user_name?, title, text, date, tags: []}]
//   session?
//   onDeletePost(postid)?
class PostList extends React.PureComponent {
  render() {
    const session = this.props.session;
    const onDeletePost = this.props.onDeletePost;

    let posts = this.props.posts.map((p) => {
      return (
        <div key={p.id}>
          <hr/>
          <Post post={p} session={session} onDeletePost={onDeletePost} /> 
        </div>
      );
    });

    return (
      <div className="post_list">
        {posts}
        <hr/>
      </div>
    );
  }
};

module.exports = PostList;
