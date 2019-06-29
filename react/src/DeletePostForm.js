const React = require("react");
const { deletePost } = require("./api");

// props:
//   id
//   session
//   onDeletePost(postid)
class DeletePostForm extends React.Component { 
  constructor(props) {
    super(props);

    this.state = {
      err_msg: null
    };

    this.handler = (e) => {
      e.preventDefault();

      const session = this.props.session;
      
      if (!session.loggedIn()) {
        throw "Must be logged in to delete post";
      }

      const token = session.token;
      const id = this.props.id;
      const onDeletePost = this.props.onDeletePost;
      const delete_el = e.target.elements["delete"];

      if (delete_el.value !== "DELETE") {
        delete_el.value = "";
        this.setState({err_msg: "Type 'DELETE' without quotes"});
        return;
      }

      deletePost(token, id).then((res) => {
        if (res.status === 200) {
          delete_el.value = "";
          onDeletePost(id);
        } else {
          this.setState({err_msg: res.msg});
        }
      });
    };
  }

  render() {
    if (!this.props.session.loggedIn()) {
      return null;
    }

    const err_msg = <span className="err_msg">{this.state.err_msg}</span>;

    return (
      <div className="delete_post_form">
        <form onSubmit={this.handler}>
          <input type="text" name="delete" placeholder="type DELETE"
                 className="delete_form_text"></input>
          <button type="submit">Delete</button>
        </form>
        {err_msg}
      </div>
    );
  }
}

module.exports = DeletePostForm;
