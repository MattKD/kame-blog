# spring-react-blog
Small multi-user blog app made with React and Spring Boot. The Spring Boot backend uses a Rest API for client requests, and stores data using Spring-JPA and PostgreSql by default.

## Building Back-end and front-end

React production build from `spring-react-blog/react/`

```
npm start 
```

React development build from `spring-react-blog/react/`

```
npm run build-dev
```

Spring build with tests from `spring-react-blog/spring/`

```
mvn package
```

Spring build without tests from `spring-react-blog/spring/`

```
mvn package -DskipTests
```

Create Postgres tables from `spring-react-blog/spring/`. This assumes Postgres is installed with a database named blogdb and user named bloguser.

```
psql -U bloguser -f create-table.sql blogdb
```

Running back-end from `spring-react-blog/spring/`

```
java -jar target/blog-0.1.0.jar
```

The React front-end must be served in some way. There is an example node server for
this in `spring-react-blog/react/server.js`, which will serve the client at 
`http://localhost:8081`.

## Spring boot back-end config
`spring-react-blog/spring/src/main/resources/application.properties`

```
# database config
spring.datasource.url
spring.datasource.username
spring.datasource.password

server.port # default 8080
server.address # default localhost

kame.enable_cors # default true
kame.cors_origins # default *
kame.disable_create_user # default false
kame.secret_key # random large (> 16 chars) key for creating sessions
kame.session_valid_days # default 30 day sessions
kame.debug_output # default true
```

To change Postgres to another database you must modify `spring-react-blog/spring/pom.xml` and change the lines

```
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```

## React front-end config
`spring-react-blog/react/src/config.js` -- default config

`spring-react-blog/react/src/config-override`
```
blog_name -- Main blog header and title
posts_per_page -- number of posts per page for pagination; default = 10
domain -- back-end server domain; default = "http://localhost:8080"
router_base -- page for blog app; default = "/blog"
```

## Rest API:

GET example using JavaScript fetch:
```
function getPosts(page, size) {
  const url = `${domain}/get_posts?page=${page}&size=${size}`;
  let status;
  return fetch(url, {
    method: "GET"
  }).then((res) => {
    status = res.status;
    return res.json();
  }).then((json) => {
    json.status = status; 
    return json;
  });
}
```

POST example using JavaScript fetch:
```
function createUser(username, password) {
  const url = `${domain}/create_user`;
  const opts = `username=${username}&password=${password}`; 
  let status;

  return fetch(url, {
    method: 'post',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: opts
  }).then((res) => {
    status = res.status;
    return res.json();
  }).then((json) => {
    json.status = status; 
    return json;
  });
}
```

Full client API usage can be found in `spring-react-blog/react/src/api.js`


Common Params:
```
page : unsigned int; default = 0
size : unsigned int; default = 0
tags : Each tag must be at most 16 chars and in regex form /^[a-zA-Z]+[a-zA-Z0-9_]*$/
       Multiple tags can be passed with repeated tags param: tags=TAG1&tags=TAG2&...
username : must be at least 2 chars and in regex form /^[a-zA-Z]+[a-zA-Z_]*$/
password : must be at least 8 chars and in regex form /^[-a-zA-Z0-9_!@#$%^&*+=)(]+$/
session : key returned from /create_user and /login
```

Response Types:
```
Msg : { msg : String }

Post : {
  id : int
  title : String
  text : String
  date : Date('yyyy-MM-dd HH:mm:ss')
  tags : [String]
}

PostList : { posts: [Post] }

UserToken : {
  id : int
  expires : Date('yyyy-MM-dd HH:mm:ss')
  token : String
}
```

Get most recent posts from all users:
```
GET: /get_posts
Params: page, size
Returns: PostList
```

Get most recent posts from a user
```
GET: /get_user_posts
Params: username, page, size
Returns: PostList | Msg
Error Codes: 403 - User not found
```

Get most recent posts from all users with having at least one included tag
```
GET: /get_posts_by_tags
Params: page, size, tags
Returns: PostList
```

Create a new user account
```
POST: /create_user
Params: username, password
Returns: UserToken | Msg
Error Codes: 400 - Username or password in bad format
             403 - Username in use
             404 - Account creation disabled
```

Login to get a session key
```
GET: /login
Params: username, password
Returns: UserToken | Msg
Error Codes: 403 - Username or password incorrect
```

Delete user account:
```
POST: /delete_user
Params: username, password
Returns: Msg
Error Codes: 403 - Username or password incorrect
```

Add a blog post
```
POST: /add_post
Params: session, title, text, tags
  title and text must not be empty or only white space
Returns: Post | Msg
Error Codes: 400 - Title, text, or tags in bad format
             403 - Invalid session token
```

Delete a blog post
```
POST: /delete_post
Params: session, postid
  postid : Post.id
Returns: Msg
Error Codes: 403 - Invalid session token | Post not found | Not your post
```
