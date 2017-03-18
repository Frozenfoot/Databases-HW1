package DB.Services;

import DB.Models.ForumThread;
import DB.Models.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by frozenfoot on 18.03.17.
 */
@Service
public class PostsService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public PostsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertPost(String slug_or_id, Post post) {
    }

    public Post getPost(int id) {
        String query = "SELECT * FROM posts WHERE id = (?)";
        return jdbcTemplate.queryForObject(query, new Object[]{id}, (rs, rowNum) -> {
            Post post = new Post(
                    rs.getString("author"),
                    rs.getTime("created").toString(),
                    rs.getString("forum"),
                    rs.getInt("id"),
                    rs.getBoolean("isEdited"),
                    rs.getString("message"),
                    rs.getInt("parent"),
                    rs.getInt("thread")
            );
            return post;
        });
    }

    public void addPost(Post post, ForumThread thread){
        String query = "SELECT forum FROM threads WHERE id = ?";
        String forum = jdbcTemplate.queryForObject(query, String.class);
        query = "INSERT INTO posts (author, created, forum, message, parent, thread)" +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                query,
                post.getAuthor(),
                Timestamp.valueOf(LocalDateTime.parse(post.getCreated())),
                forum,
                post.getMessage(),
                post.getParent(),
                post.getThread()
                );
    }

    public void changePost(Post post, int id){
        String query = "UPDATE posts" +
                " SET message = ?, isEdited = true " +
                "WHERE id = ?";
        jdbcTemplate.update(query, new Object[]{post.getMessage(), id});
    }

    public List<Post> getLastPosts(int length, ForumThread thread){
        String query = "SELECT DISTINCT *" +
                " FROM posts" +
                " WHERE posts.thread = LOWER(?)" +
                " ORDER BY id" +
                " DESC LIMIT 0, ?";
        return jdbcTemplate.queryForList(query, Post.class, thread.getId(), length);
    }

    public List<Post> getFlatPosts(String slug, int limit, int pageId, Boolean desc){
        String query = "SELECT * FROM posts" +
                " JOIN threads ON (threads.id = posts.thread AND threads.slug = ?)" +
                " JOIN forums ON threads.forum = forums.slug" +
                " JOIN users ON posts.author = users.nickname " +
                " ORDER BY created " + (desc ? "DESC" : "") + "LIMIT ? OFFSET ?";
        return jdbcTemplate.query(query,
                (rs, rowNum) -> {
                    Post post = new Post(
                            rs.getString("author"),
                            rs.getTime("created").toString(),
                            rs.getString("forum"),
                            rs.getInt("id"),
                            rs.getBoolean("isEdited"),
                            rs.getString("message"),
                            rs.getInt("parent"),
                            rs.getInt("thread")
                    );
                    return post;
                },
                slug,
                limit,
                pageId);
    }

    public List<Post> getTreePosts(String slug, int limit, int pageId, Boolean desc){
        String query = "WITH RECURSIVE tree (author, created, forum, id, isEdited, message, parent, thread, posts_)" +
                " AS " +
                " (SELECT author, created, forum, id, isEdited, message, parent, thread, array[id] " +
                " FROM posts WHERE parent = 0 " +
                " UNION ALL " +
                " SELECT p.author, p.created, p.forum, p.id, p.isEdited, p.message, p.parent, p.thread, array_append(posts_, p.id)" +
                " FROM posts p " +
                " JOIN tree ON tree.id = p.parent) " +
                " SELECT tree_.id, nickname, tr.created, f.slug, isEdited, tr.message, tr.parent, tr.thread, aray_to_string(posts, ' ')" +
                " AS posts_ FROM tree tr " +
                " JOIN threads t ON (tr.thread = threads.id AND t.slug = ?) " +
                " JOIN forums f ON t.forum = forums.slug " +
                " JOIN users u ON u.nickname = tr.nickname " +
                " ORDER BY posts_ " + (desc ? "DESC" : "") + "LIMIT ? OFFSET ?";
        return jdbcTemplate.query(
                query,
                (rs, rowNum) -> {
                    Post post = new Post(
                            rs.getString("author"),
                            rs.getTime("created").toString(),
                            rs.getString("forum"),
                            rs.getInt("id"),
                            rs.getBoolean("isEdited"),
                            rs.getString("message"),
                            rs.getInt("parent"),
                            rs.getInt("thread")
                    );
                    return post;
                },
                slug,
                limit,
                pageId
                );
    }

    public List<Integer> getParents(String slug, int limit, int pageId, Boolean desc){
        String query = "SELECT posts.id FROM posts " +
                " JOIN threads ON threads.id = forums.thread " +
                " WHERE parent = 0 AND threads.slug = ? " +
                " ORDER BY posts.id, " + (desc ? "DESC " : "") + "LIMIT ? OFFSET ?";
        return jdbcTemplate.queryForList(query, new Object[]{slug, limit, pageId}, Integer.class);
    }

    public List<Post> getParentTreePosts(String slug, List<Integer> parents, Boolean desc){
        List<Post> result = new ArrayList<>();
        String query = "WITH RECURSIVE tree (author, created, forum, id, isEdited, message, parent, thread, posts_)" +
                " AS " +
                " (SELECT author, created, forum, id, isEdited, message, parent, thread, array[id] " +
                " FROM posts WHERE id = ? " +
                " UNION ALL " +
                " SELECT p.author, p.created, p.forum, p.id, p.isEdited, p.message, p.parent, p.thread, array_append(posts_, p.id)" +
                " FROM posts p " +
                " JOIN tree ON tree.id = p.parent) " +
                " SELECT tree_.id, nickname, tr.created, f.slug, isEdited, tr.message, tr.parent, tr.thread, aray_to_string(posts, ' ')" +
                " AS posts_ FROM tree tr " +
                " JOIN threads t ON (tr.thread = threads.id AND t.slug = ?) " +
                " JOIN forums f ON t.forum = forums.slug " +
                " JOIN users u ON u.nickname = tr.nickname " +
                " ORDER BY posts_ " + (desc ? "DESC" : "");
        for (Integer parentId : parents){
            result.addAll(jdbcTemplate.query(
                    query,
                    (rs, rowNum) -> {
                        Post post = new Post(
                                rs.getString("author"),
                                rs.getTime("created").toString(),
                                rs.getString("forum"),
                                rs.getInt("id"),
                                rs.getBoolean("isEdited"),
                                rs.getString("message"),
                                rs.getInt("parent"),
                                rs.getInt("thread")
                        );
                        return post;
                    },
                    parentId,
                    slug
            ));
        }
        return result;
    }
}