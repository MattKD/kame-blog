const { domain } = require("./config");

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

function getUserPosts(username, page, size) {
  const url = `${domain}/get_user_posts` +
              `?username=${username}&page=${page}&size=${size}`;
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

function getPostsByTags(tags, page, size) {
  let url = `${domain}/get_posts_by_tags?page=${page}&size=${size}`;
  for (let tag of tags) {
    url += "&tags=" + tag;
  }

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

function login(username, password) {
  const url = `${domain}/login?username=${username}&password=${password}`;
  let status;
  return fetch(url, {
    method: 'GET',
  }).then((res) => {
    status = res.status;
    return res.json();
  }).then((json) => {
    json.status = status; 
    return json;
  });
}

function addPost(session, title, text, tags) {
  const url = `${domain}/add_post`;
  const opts = `session=${session}&title=${title}&text=${text}` +
                tags.map((tag) => `&tags=${tag}`).join("");
  let status;
  return fetch(url, {
    method: 'POST',
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

function deletePost(session, postid) {
  const url = `${domain}/delete_post`;
  const opts = `session=${session}&id=${postid}`
  let status;

  return fetch(url, {
    method: 'POST',
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

function deleteUser(username, password) {
  const url = `${domain}/delete_user`;
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


module.exports.getPosts = getPosts;
module.exports.getUserPosts = getUserPosts;
module.exports.getPostsByTags = getPostsByTags;
module.exports.login = login;
module.exports.addPost = addPost;
module.exports.deletePost = deletePost;
module.exports.createUser = createUser;
module.exports.deleteUser = deleteUser;
