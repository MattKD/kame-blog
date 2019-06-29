const React = require("react");
const { withRouter } = require("react-router-dom");
const { combineTags, splitUserTagsStr } = require("./util");

const TagSearchForm = withRouter((props) => { 
  const tagsMap = (tags_str) => {
    // replace space seperator with '+'
    const tags = combineTags(splitUserTagsStr(tags_str));
    return "/tags/" + tags;
  };

  const history = props.history;

  const handler = e => {
    e.preventDefault();

    const tags_str = e.target.elements["tags_str"].value;
    e.target.elements["tags_str"].value = "";
    const link = tagsMap(tags_str);
    history.push(link);      
  };
 
  return (
    <form onSubmit={handler}>
      <label>Search for posts with tags</label>
      <input type="text" name="tags_str" 
             placeholder="tag1 tag2 tag3"></input>
    </form>
  );
});

module.exports = TagSearchForm;
