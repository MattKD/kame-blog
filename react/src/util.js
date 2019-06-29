function getCookie(name) {
  let c = document.cookie;
  let i = c.indexOf(name + "=");
  if (i === -1) {
    return undefined;
  }
  c = c.substring(i);
  i = c.indexOf("=");
  let i2 = c.indexOf(";");
  if (i2 === -1) {
    i2 = c.length;
  }
  c = c.substring(i+1, i2);
  return c;
}

// covert list of tags to string for tags page url
function combineTags(tag_list) {
  return tag_list.join("+");
}

// convert tags string from tags page url to list of tags
function splitTagsStr(tags_str) {
  return tags_str.split("+");
}

// convert tags string from tags search form to list of tags
function splitUserTagsStr(tags_str) {
  // replace space seperator with '+'
  return tags_str.trim().split(/[ \t\n]/).filter(t => t !== "");
};

// check if tag is in valid format for backend
function tagIsValid(tag) {
  return tag.match(/^[a-zA-Z]+[a-zA-Z0-9_]*$/) !== null;
}

// check if password is in valid format for backend
function passwordIsValid(pw) {
  return pw.length > 7 && pw.match(/^[-a-zA-Z0-9_!@#$%^&*+=)(]+$/) !== null;
}

// check if username is in valid format for backend
function usernameIsValid(name) {
  return name.length > 1 && name.match(/^[a-zA-Z]+[a-zA-Z_]*$/) !== null;
}

module.exports.getCookie = getCookie;
module.exports.combineTags = combineTags;
module.exports.splitTagsStr = splitTagsStr;
module.exports.splitUserTagsStr = splitUserTagsStr;
module.exports.tagIsValid = tagIsValid;
module.exports.passwordIsValid = passwordIsValid;
module.exports.usernameIsValid = usernameIsValid;
