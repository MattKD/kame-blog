const React = require("react");
const { login } = require("./api");

// props:
//   session
class LoginForm extends React.Component { 
  constructor(props) {
    super(props);

    this.state = {
      login_failed: false
    };

    this.handler = (e) => {
      e.preventDefault();
      const session = this.props.session;
      const username_el = e.target.elements['username'];
      const password_el = e.target.elements['password'];
      const username = username_el.value;
      const password = password_el.value;

      login(username, password).then((token) => {
        if (token.status !== 200) {
          this.setState({login_failed: true});
        } else { 
          username_el.value = "";
          password_el.value = "";
          this.setState({login_failed: false});
          session.login(username, token.id, token.token, token.expires);
        }
      });
    };
  }

  render() {
    const login_failed = this.state.login_failed;
    const err_msg = login_failed  
      ? <div className="err_msg">Username or password wrong</div>
      : null;

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
