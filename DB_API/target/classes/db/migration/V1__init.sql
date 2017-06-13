CREATE EXTENSION IF NOT EXISTS CITEXT;

CREATE TABLE IF NOT EXISTS users(
  about TEXT,
  email CITEXT NOT NULL UNIQUE,
  fullname TEXT,
  nickname CITEXT UNIQUE PRIMARY KEY
);

CREATE INDEX ON users (lower(nickname COLLATE "ucs_basic"));

CREATE TABLE IF NOT EXISTS forums(
  id SERIAL NOT NULL PRIMARY KEY,
  posts BIGINT DEFAULT 0 NOT NULL,
  threads BIGINT DEFAULT 0 NOT NULL,
  slug CITEXT UNIQUE NOT NULL,
  title TEXT NOT NULL,
  user_ CITEXT NOT NULL REFERENCES users(nickname)
);

CREATE INDEX ON forums (lower(slug));
CREATE INDEX ON forums (user_);

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

CREATE INDEX ON threads (slug);
CREATE INDEX ON threads (lower(forum));

CREATE TABLE IF NOT EXISTS votes(
  thread INTEGER NOT NULL,
  voice INTEGER NOT NULL,
  nickname CITEXT NOT NULL REFERENCES users(nickname),
  UNIQUE (thread, nickname)
);

CREATE INDEX ON votes (nickname, thread);

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

CREATE INDEX ON posts (author,forum);
CREATE INDEX ON posts (id, parent, thread);
CREATE INDEX ON posts (thread, id);
CREATE INDEX ON posts ((posts.array_for_tree[1]), id);
CREATE INDEX ON posts (thread, array_for_tree);
CREATE INDEX ON posts (thread, created, id);

CREATE TABLE IF NOT EXISTS users_in_forum (
  user_ CITEXT NOT NULL,
  forum_slug CITEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_forum_users_user ON users_in_forum (user_);
CREATE INDEX IF NOT EXISTS idx_forum_users_forum ON users_in_forum (forum_slug);
CREATE INDEX IF NOT EXISTS idx_forum_users_both ON users_in_forum (lower(forum_slug), user_);
