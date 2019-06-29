const React = require("react");
const { withRouter } = require("react-router-dom");
const { deleteUser } = require("./api");

// props:
//   session 
//   onDeleteUser(userid, name)
const DeleteUserForm  = withRouter(class extends React.Component { 
  constructor(props) {
    super(props);

    this.state = {
      err_msg: null
    };

    this.handler = (e) => {
      e.preventDefault();
      const session = this.props.session;
      const history = this.props.history;

      if (!session.loggedIn()) {
        throw "Must be logged in to delete user";
      }

      const username = session.name;
      const password_el = e.target.elements["password"];
      const password = password_el.value;

      deleteUser(username, password).then((res) => {
        if (res.status === 200) {
          password_el.value = "";
          this.setState({err_msg: null});
          session.logout();
          this.props.onDeleteUser(session.id, session.name);
          history.push("/");

        } else { 
          const err_msg = "Server Error (" + msg.status + "): " + msg.msg;
          this.setState({err_msg: err_msg});
        }
      });
    };
  }

  render() {
    if (!this.props.session.loggedIn()) {
      return null;
    }

    const err_msg  = this.state.err_msg;
    const err_span = err_msg 
                   ? <span className="err_msg">{err_msg}</span> : null;

    return (
      <div className="delete_user_form">
        <form onSubmit={this.handler}>
          <input type="password" name="password" placeholder="password"
                 className="delete_user_form_pw"></input>
          <button type="submit">Delete Account</button>
        </form>
        {err_msg}
      </div>
    );
  }
})

module.exports = DeleteUserForm;
