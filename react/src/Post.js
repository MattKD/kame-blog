const React = require("react");
const { Link } = require("react-router-dom");
const DeletePostForm = require("./DeletePostForm");

// props:
//   post: id, user_name?, title, text, date, tags: []
//   session?
//   onDeletePost(postid)?
class Post extends React.PureComponent {
  render() {
    const session = this.props.session;
    const post = this.props.post;
    const title = post.title;
    const user = post.user_name;
    const user_link = "/users/" + user;
    const text = post.text;
    const date = post.date;
    const onDeletePost = this.props.onDeletePost;

    const tag_style = {
      paddingRight: "0px",
      //marginRight: "0px",
      textDecoration: "none"
    };
    const title_style = {
      display: "inline",
    };

    const tags = post.tags.map((tag, i) => {
      const link = 
        <Link to={"/tags/"+tag} style={tag_style}>{tag}</Link>;
      const sep = i === post.tags.length - 1 ? null : " | ";
      return <span key={tag}>{link}{sep}</span>;
    });

    const user_link_tag = user ? 
      <span><Link to={user_link}>{user}</Link> - </span> :
      null;

    const delete_post_form = onDeletePost ? 
      <DeletePostForm session={session} id={post.id} 
                      onDeletePost={onDeletePost}/> : 
      null;

    return (
      <div> 
        {user_link_tag}
        <h3 style={title_style}>{title}</h3>
        <p>{text}</p>
        <div>{tags}</div>
        <span>{date}</span>
        {delete_post_form}
      </div>
    );
  }
};

module.exports = Post;
