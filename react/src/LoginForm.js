const React = require("react");
const { login } = require("./api");

// props:
//   session
class LoginForm extends React.Component { 
  constructor(props) {
    super(props);

    this.state = {
      err_msg: null
    };

    this.handler = (e) => {
      e.preventDefault();
      const session = this.props.session;
      const username_el = e.target.elements['username'];
      const password_el = e.target.elements['password'];
      const username = username_el.value;
      const password = password_el.value;

      login(username, password).then((res) => {
        if (res.status !== 200) {
          this.setState({err_msg: res.msg});
        } else { 
          username_el.value = "";
          password_el.value = "";
          this.setState({err_msg: null});
          session.login(username, res.id, res.token, res.expires);
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
    const err_msg = this.state.err_msg ?
      <div className="err_msg">{this.state.err_msg}</div> :
      null;

    return (
      <div>
        <form onSubmit={this.handler}>
          <input type="text" name="username" placeholder="username"></input>
          <input type="password" name="password" placeholder="password"></input>
          <button type="submit">Login</button>
        </form>
        {err_msg}
      </div>
    );
  }
}

module.exports = LoginForm;
