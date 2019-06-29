const React = require("react");
const { Link } = require("react-router-dom");
const { getPostsByTags } = require("./api");
const PostList = require("./PostList");
const PageHeader = require("./PageHeader");
const TagSearchForm = require("./TagSearchForm");
const { splitTagsStr, combineTags } = require("./util");
const { posts_per_page } = require("./config");

// props:
//   session
//   tag_posts
//   updateTagPosts(tag_posts)
class TagsPage extends React.PureComponent {
  constructor(props) {
    super(props);
    this.getPostsByTags = this.getPostsByTags.bind(this);
  }

  getPostsByTags(tags, page) {
    const tag_posts = this.props.tag_posts;

    getPostsByTags(tags, page, posts_per_page).then((res) => {
      const tags_str = combineTags(tags);
      if (tag_posts[tags_str] === undefined) {
        tag_posts[tags_str] = {};
      }
      tag_posts[tags_str][page] = res.posts;
      this.props.updateTagPosts(tag_posts);
    });
  }

  render() {
    const session = this.props.session;
    const tag_posts = this.props.tag_posts;
    const getPostsByTags = this.getPostsByTags;
    const params = this.props.match.params;
    const page = params.page ? parseInt(params.page) : 0;
    const tags = params.tags;
    const tags_list = splitTagsStr(tags);

    document.title = "Posts with tags: " + tags;

    if (tag_posts[tags] === undefined || tag_posts[tags][page] === undefined) {
      getPostsByTags(tags_list, page);
      return null;
    }

    const posts = tag_posts[tags][page];
    let header = "There is no posts to show with tags: " + tags;
    if (posts.length > 0) {
      const start = page * posts_per_page + 1;
      const end = start + posts.length - 1;
      header = `Showing posts with tags: ${tags} (${start} to ${end})`;
    }

    const has_next_page = posts.length === posts_per_page ? true : false;
    const next_page_link = has_next_page 
          ? <Link to={`/tags/${tags}/${page+1}`}
                  className="foot_link">Next Page</Link> 
          : undefined;
    const prev_page_link = page > 0 ?
          <Link to={`/tags/${tags}/${page-1}`} 
                className="foot_link">Prev Page</Link> : 
          undefined;

    return (
      <div>
        <PageHeader session={session} />
        <TagSearchForm />
        <h2>{header}</h2>
        <PostList posts={posts} />
        {prev_page_link}
        {next_page_link}
      </div>
    );
  }
}

module.exports = TagsPage;
