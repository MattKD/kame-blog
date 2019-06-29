const React = require("react");
const PageHeader = require("./PageHeader");

// props:
//   session
function ErrorPage(props) {
  document.title = "Page Not Found";
  return (
    <div>
      <PageHeader session={props.session} disable_login />
      <h2>Page Not Found</h2>
    </div>
  )
}

module.exports = ErrorPage;
