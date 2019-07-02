
let config = {};
try {
  config = require('./config-override');
} catch(e) { }

module.exports = {
  blog_name: config.blog_name || "Kame-Blog",
  posts_per_page: config.posts_per_page || 10,
  domain: config.domain || "http://localhost:8080", // api domain
  router_base: config.router_base || "/" 
}

