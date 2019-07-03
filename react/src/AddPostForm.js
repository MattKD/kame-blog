const React = require("react");
const { withRouter } = require("react-router-dom");
const { addPost } = require("./api");
const { splitUserTagsStr, tagIsValid } = require("./util");

//Props:
//  session: loggedIn(), token
//  onAddPosts(post_response)
class AddPostForm extends React.Component { 
  constructor(props) {
    super(props);

    this.state = {
      err_msg: null
    };

    this.handler = (e) => {
      e.preventDefault();
      const session = this.props.session;
      if (!session.loggedIn()) {
        throw "Must be logged in to add posts";
      }
        
      const token = session.token;
      const title_el = e.target.elements["title"];
      const text_el = e.target.elements["text"];
      const tags_el = e.target.elements["tags"];
      const title = title_el.value.trim();
      const text = text_el.value.trim();
      const tags = splitUserTagsStr(tags_el.value);

      if (title === "") {
        this.setState({err_msg: "Error: Invalid title"});
        return;
      }
      if (text === "") {
        this.setState({err_msg: "Error: Invalid text"});
        return;
      }
      for (let tag of tags) {
        if (!tagIsValid(tag)) {
          this.setState({err_msg: "Error: Invalid tags"});
          return;
        }
      }

      addPost(token, title, text, tags).then((res) => {
        if (res.status === 200) {
          title_el.value = "";
          text_el.value = "";
          tags_el.value = "";
          this.setState({err_msg: null});
          this.props.onAddPost(res);
        } else { 
          this.setState({err_msg: res.msg});
        }
      }).catch((err) => {
        console.log(err);
        this.setState({
          err_msg: "A server error occurred. Try again in a few minutes"
        });
      });
    };
  }

  render() {
    if (!this.props.session.loggedIn()) {
      return null;
    }

    const err_msg = this.state.err_msg;
    const err_span = err_msg 
                   ? <span className="err_msg">{err_msg}</span> : null;

    return (
      <div className="post_form">
        <form onSubmit={this.handler}>
          <input type="text" name="title" placeholder="Post Title"
                 className="post_form_title"></input>
          <div>
            <textarea name="text" placeholder="Post text" rows="10" 
                      className="post_form_text"></textarea>
          </div>
          <input type="text" name="tags" placeholder="tag1 tag2 tag3"
                 className="post_form_tags"></input>
          <button type="submit">Submit Post</button>
        </form>
        {err_span} 
      </div>
    );
  }
}

module.exports = AddPostForm;
