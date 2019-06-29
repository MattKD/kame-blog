const React = require("react");
const { Redirect } = require("react-router-dom");
const { createUser } = require("./api");
const { usernameIsValid, passwordIsValid } = require("./util");
const { blog_name } = require("./config");
const PageHeader = require("./PageHeader");
const CreateUserForm = require("./CreateUserForm");

// props:
//   session 
class CreateUserPage extends React.Component {
  render() {
    const session = this.props.session;

    if (session.loggedIn()) {
      const userpage = "/users/" + session.name;
      return <Redirect to={userpage} />;
    }

    document.title = blog_name + " - Create User";
    const name_rules = "Username must be at least 2 characters; " +
                       "start with a letter; and contains only letters and " +
                       "underscores."
    const pw_rules = "Password must be at least 8 characters; " +
                     "and can't contain spaces."

    return (
      <div>
        <PageHeader session={session} disable_login />
        <h3>Create User Account</h3>
        <CreateUserForm session={session} />
        <p>{name_rules}</p>
        <p>{pw_rules}</p>
      </div>
    );
  }
}

module.exports = CreateUserPage;
