package DB.Services;

import DB.Models.ForumThread;
import DB.Models.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
}