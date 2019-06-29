const React = require("react");
const { Link } = require("react-router-dom");
const { blog_name } = require("./config");
const LoginForm = require("./LoginForm");

// props:
//   session
//   disable_login
function PageHeader(props) {
  const session = props.session;
  const disable_login = props.disable_login;
  const logged_in = session.loggedIn();
  const name = logged_in ? session.name : null;

  const logout = () => {
    session.logout();
  };

  const logout_link = 
    <span className="fake_link" onClick={logout}>Logout</span>;

  const login_out = disable_login ? null :
                    logged_in ? logout_link : 
                    <LoginForm session={session} />;

  const create_user = disable_login || logged_in ? null :
                      <Link to="/create_user">Create Account</Link>;

  const user = logged_in ? 
               <Link to={"/users/"+name}>{name}</Link> : null;

  return (
    <div>
      <div>
        <h2 className="inline_header"><Link to="/">{blog_name}</Link></h2>
        <span className="inline_header_right">{create_user}</span>
      </div>
      <div>
        <span className="inline_header_right">{login_out}</span>
        <span className="inline_header_right">{user}</span>
      </div>
      <br/>
    </div>
  );
}


module.exports = PageHeader;
