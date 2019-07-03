require("babel-polyfill");
require("isomorphic-fetch");
const React = require("react");
const ReactDOM = require("react-dom");
const { BrowserRouter: Router, Route, Switch } = require("react-router-dom");
const IndexPage = require("./IndexPage");
const UserPage = require("./UserPage");
const TagsPage = require("./TagsPage");
const CreateUserPage = require("./CreateUserPage");
const ErrorPage = require("./ErrorPage");
const { getCookie } = require("./util");
const { router_base } = require("./config");

function cookieLoggedIn() {
  const name = getCookie("name");
  const id = getCookie("id");
  const token = getCookie("token");
  return {name, id, token};
}

function cookieLogin(name, id, token, expires) {
  if (id && token && expires) {
    const expires_date = new Date(expires);
    const cookie_opts = "; expires=" + expires_date.toGMTString() + "; path=/";
    document.cookie = "name=" + name + cookie_opts;
    document.cookie = "id=" + id + cookie_opts;
    document.cookie = "token=" + token + cookie_opts;
  } 
}

function cookieLogout() {
  const cookie_opts = "; expires=Thu, 01 Jan 1970 00:00:01 GMT; path=/";
  document.cookie = "name=" + cookie_opts;
  document.cookie = "id=" + cookie_opts;
  document.cookie = "token=" + cookie_opts;
}

class App extends React.Component {
  constructor(props) {
    super(props);

    this.login = this.login.bind(this);
    this.logout = this.logout.bind(this);
    this.updateHomePosts = this.updateHomePosts.bind(this);
    this.updateUserPosts = this.updateUserPosts.bind(this);
    this.updateTagPosts = this.updateTagPosts.bind(this);
    this.clearPosts = this.clearPosts.bind(this);

    const session = cookieLoggedIn();

    this.state = {
      home_posts: {},
      user_posts: {},
      tag_posts: {},
      session: this.createSession(session.name, session.id, session.token),
      server_err: undefined
    };
  }

  createSession(name, id, token) {
    return {
      login: this.login,
      logout: this.logout,
      loggedIn: function() { return this.id != undefined; },
      name,
      id,
      token
    }
  }

  login(name, id, token, expires) {
    cookieLogin(name, id, token, expires);
    this.setState({
      session: this.createSession(name, id, token)
    });
  }

  logout() {
    cookieLogout();
    this.setState({
      session: this.createSession()
    });
  }

  clearPosts(dummy) {
    this.setState({
      home_posts: {},
      user_posts: {},
      tag_posts: {}
    });
  }

  updateHomePosts(home_posts) {
    if (home_posts === this.state.home_posts) {
      home_posts = Object.assign({}, home_posts)
    }
    this.setState({
      home_posts 
    });
  }

  updateUserPosts(user_posts) {
    if (user_posts === this.state.user_posts) {
      user_posts = Object.assign({}, user_posts)
    }
    this.setState({
      user_posts 
    });
  }

  updateTagPosts(tag_posts) {
    if (tag_posts === this.state.tag_posts) {
      tag_posts = Object.assign({}, tag_posts)
    }
    this.setState({
      tag_posts 
    });
  }

  render() {
    const session = this.state.session;
    const home_posts = this.state.home_posts;
    const user_posts = this.state.user_posts;
    const tag_posts = this.state.tag_posts;
    const updateHomePosts = this.updateHomePosts;
    const updateUserPosts = this.updateUserPosts;
    const updateTagPosts = this.updateTagPosts;
    const clearPosts = this.clearPosts;
    const server_err = this.state.server_err;

    const createUserRender = (props) => (
      <CreateUserPage {...props} session={session} />
    );
    const indexRender = (props) => (
      <IndexPage {...props} session={session} home_posts={home_posts} 
                 updateHomePosts={updateHomePosts}/>
    );
    const userRender = (props) => (
      <UserPage {...props} session={session} user_posts={user_posts} 
                 updateUserPosts={updateUserPosts} onAddPost={clearPosts}
                 onDeleteUser={clearPosts} onDeletePost={clearPosts} />
    );
    const tagsRender = (props) => (
      <TagsPage {...props} session={session} tag_posts={tag_posts} 
                 updateTagPosts={updateTagPosts} />
    );
    const errorRender = (props) => (
      <ErrorPage {...props} session={session} />
    );

    return (
      <Router basename={router_base}>
        <Switch>
          <Route path="/create_user" exact render={createUserRender} />
          <Route path="/" exact render={indexRender} />
          <Route path="/:page" exact render={indexRender} />
          <Route path="/users/:username" exact render={userRender} />
          <Route path="/users/:username/:page" exact render={userRender} />
          <Route path="/tags/:tags" exact render={tagsRender} />
          <Route path="/tags/:tags/:page" exact render={tagsRender} />
          <Route render={errorRender} />
        </Switch>
      </Router>
    );
  }
}


const rootElem = document.getElementById("root");

ReactDOM.render(<App/>, rootElem);

