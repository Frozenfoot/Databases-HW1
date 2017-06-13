CREATE EXTENSION IF NOT EXISTS CITEXT;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS forums;
DROP TABLE IF EXISTS threads;
DROP TABLE IF EXISTS votes;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS users_in_forum;

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
  user_ CITEXT NOT NULL,
  FOREIGN KEY (user_) REFERENCES users(nickname)
);

CREATE INDEX ON forums (lower(slug));
CREATE INDEX ON forums (user_);

CREATE TABLE IF NOT EXISTS threads(
  author CITEXT NOT NULL,
  created TIMESTAMP,
  forum CITEXT NOT NULL,
  id SERIAL PRIMARY KEY,
  message TEXT,
  slug CITEXT UNIQUE,
  title TEXT NOT NULL,
  votes BIGINT DEFAULT 0 NOT NULL,
  FOREIGN KEY (author) REFERENCES users(nickname),
  FOREIGN KEY (forum) REFERENCES  forums(slug)
);
--
CREATE INDEX ON threads (slug);
CREATE INDEX ON threads (lower(forum));

CREATE TABLE IF NOT EXISTS votes(
  thread INTEGER NOT NULL,
  voice INTEGER NOT NULL,
  nickname CITEXT NOT NULL,
  UNIQUE (thread, nickname),
  FOREIGN KEY (nickname) REFERENCES users(nickname)
);
--
CREATE INDEX ON votes (nickname, thread);

CREATE TABLE IF NOT EXISTS posts(
  author CITEXT NOT NULL,
  created TIMESTAMP,
  forum CITEXT NOT NULL,
  id SERIAL PRIMARY KEY,
  isEdited BOOLEAN DEFAULT FALSE,
  message TEXT NOT NULL ,
  parent INTEGER DEFAULT 0,
  thread INTEGER,
  array_for_tree INTEGER[],
  FOREIGN KEY (author) REFERENCES users(nickname),
  FOREIGN KEY (forum) REFERENCES forums(slug),
  FOREIGN KEY (thread) REFERENCES threads(id)
);
--
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
--
CREATE INDEX IF NOT EXISTS idx_forum_users_user ON users_in_forum (user_);
CREATE INDEX IF NOT EXISTS idx_forum_users_forum ON users_in_forum (forum_slug);
CREATE INDEX IF NOT EXISTS idx_forum_users_both ON users_in_forum (lower(forum_slug), user_);
