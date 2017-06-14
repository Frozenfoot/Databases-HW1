CREATE EXTENSION IF NOT EXISTS CITEXT;

CREATE TABLE IF NOT EXISTS users(
  about TEXT,
  email CITEXT NOT NULL UNIQUE,
  fullname TEXT,
  nickname CITEXT UNIQUE PRIMARY KEY
);

CREATE INDEX IF NOT EXISTS index_users_nickname ON users (lower(nickname));

CREATE TABLE IF NOT EXISTS forums(
  id SERIAL NOT NULL PRIMARY KEY,
  posts BIGINT DEFAULT 0 NOT NULL,
  threads BIGINT DEFAULT 0 NOT NULL,
  slug CITEXT UNIQUE NOT NULL,
  title TEXT NOT NULL,
  user_ CITEXT NOT NULL REFERENCES users(nickname)
);

CREATE INDEX IF NOT EXISTS index_forums_user ON forums (user_);

CREATE TABLE IF NOT EXISTS threads(
  author CITEXT NOT NULL REFERENCES users(nickname),
  created TIMESTAMP,
  forum CITEXT NOT NULL REFERENCES forums(slug),
  id SERIAL PRIMARY KEY,
  message TEXT,
  slug CITEXT UNIQUE,
  title TEXT NOT NULL,
  votes BIGINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS index_threads_slug ON threads (slug);

CREATE TABLE IF NOT EXISTS votes(
  thread INTEGER NOT NULL,
  voice INTEGER NOT NULL,
  nickname CITEXT NOT NULL REFERENCES users(nickname),
  UNIQUE (thread, nickname)
);

CREATE INDEX IF NOT EXISTS index_votes_nickname_thread ON votes (nickname, thread);

CREATE TABLE IF NOT EXISTS posts(
  author CITEXT NOT NULL REFERENCES users(nickname),
  created TIMESTAMP,
  forum CITEXT NOT NULL REFERENCES forums(slug),
  id SERIAL PRIMARY KEY,
  isEdited BOOLEAN DEFAULT FALSE,
  message TEXT NOT NULL ,
  parent INTEGER DEFAULT 0,
  thread INTEGER REFERENCES threads(id),
  array_for_tree INTEGER[]
);


CREATE INDEX IF NOT EXISTS index_posts_a_f ON posts (author,forum);
CREATE INDEX IF NOT EXISTS index_posts_i_p_t ON posts (id, parent, thread);
CREATE INDEX IF NOT EXISTS index_posts_t_i ON posts (thread, id);
CREATE INDEX IF NOT EXISTS index_posts_a_i ON posts ((posts.array_for_tree[1]), id);
CREATE INDEX IF NOT EXISTS index_posts_t_a ON posts (thread, array_for_tree);
CREATE INDEX IF NOT EXISTS index_posts_t_c_i ON posts (thread, created, id);

CREATE TABLE IF NOT EXISTS users_in_forum (
  user_ CITEXT NOT NULL,
  forum_slug CITEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS index_users_in_forum_user_ ON users_in_forum (user_);
CREATE INDEX IF NOT EXISTS index_users_in_forum_forum ON users_in_forum (forum_slug);
CREATE INDEX IF NOT EXISTS index_users_in_forum__user_forum ON users_in_forum (lower(forum_slug), user_);
