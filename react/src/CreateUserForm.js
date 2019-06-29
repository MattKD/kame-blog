const React = require("react");
const { createUser } = require("./api");
const { usernameIsValid, passwordIsValid } = require("./util");

// props:
//   session
class CreateUserForm extends React.Component { 
  constructor(props) {
    super(props);

    this.state = {
      err_msg: null
    };

    this.handler = (e) => {
      e.preventDefault();
      const session = this.props.session;
      const username_el = e.target.elements["username"];
      const password_el = e.target.elements["password"];
      const password2_el = e.target.elements["password2"];
      const username = username_el.value;
      const password = password_el.value;
      const password2 = password2_el.value;

      if (!usernameIsValid(username)) {
        this.setState({err_msg: "Error: username has bad format"})
        return;
      }
      if (!passwordIsValid(password)) {
        this.setState({err_msg: "Error: password has bad format"})
        return;
      }
      if (password !== password2) {
        this.setState({err_msg: "Error: passwords don't match"})
        return;
      }

      createUser(username, password).then((res) => {
        if (res.status === 200) {
          username_el.value = "";
          password_el.value = "";
          password2_el.value = "";
          this.setState({err_msg: null});
          session.login(username, res.id, res.token, res.expires);
        } else { 
          this.setState({err_msg: res.msg});
        }
      });
    };
  }

  render() {
    const err_msg  = this.state.err_msg;
    const err_span = err_msg 
                   ? <span className="err_msg">{err_msg}</span> : null;

    return (
      <div className="create_user_form">
        <form onSubmit={this.handler}>
          <input type="text" name="username" placeholder="username"
                 className="create_user_form_name"></input>
          <input type="password" name="password" placeholder="password"
                 className="create_user_form_pw"></input>
          <input type="password" name="password2" placeholder="password"
                 className="create_user_form_pw2"></input>
          <button type="submit">Create</button>
        </form>
        {err_span}
      </div>
    );
  }
}

module.exports = CreateUserForm;
