
create table if not exists users (
  id serial primary key,
  name text not null unique,
  password char(64) not null,
  salt char(16) not null
);

create table if not exists posts (
  id serial primary key,
  title text not null,
  post_text text not null,
  post_date timestamp not null,
  user_id int references users on delete restrict
);

create table if not exists tags (
  id serial primary key,
  name text not nulL unique
);

create table if not exists post_tags (
  post_id int references posts,
  tag_id int references tags,
  primary key (post_id, tag_id)
);


