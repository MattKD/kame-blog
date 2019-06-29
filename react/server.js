const fs = require("fs");
const express = require("express");
let config = require("./default_server_config");

function printUsage() {
  console.log("Usage: node server [-c CONFIG_NAME]");
}

for (let i = 2; i < process.argv.length; i++) {
  const arg = process.argv[i];
  console.log(arg);
  switch (arg) {
    case '-c': 
      i += 1;
      if (i === process.argv.length) {
        console.log("Error: missing config filename after '-c'");
        printUsage();
        return;
      }
      const config_name = process.argv[i];
      console.log("Using config file: " + config_name);
      Object.assign(config, require("./" + config_name));
      break;
    default:
      console.log("Error: Unknown argument '" + arg + "'");
      printUsage();
      return;
  }
}

const { domain, host, port } = config;
const app = express();
app.use(express.static("public"));

const index_html = 
 `
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <base href="${domain}">
    <link rel="stylesheet" href="css/style.css">
  </head>
  <body>
    <div id="root"></div>
    <script src="js/index.js"></script>
  </body>
</html>
`;

app.get("/*", 
  function(req, res) {
    res.send(index_html);
  }
);

console.log("NODE_ENV: " + process.env.NODE_ENV);

var server = app.listen(port, host, function() {
  const host = server.address().address;
  const port = server.address().port;
  console.log("Server listening at http://%s:%s", host, port);
});


