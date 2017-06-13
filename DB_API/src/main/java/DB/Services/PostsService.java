package DB.Services;

import DB.Models.ForumThread;
import DB.Models.Post;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by frozenfoot on 18.03.17.
 */
@Service
@Transactional
public class PostsService {
    @Autowired
    JdbcTemplate jdbcTemplate;
    private static final RowMapper<Integer> parentMapper = (rs, num) -> rs.getInt("id");

    public PostsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Post getPost(int id) {
        String query = "SELECT * FROM posts WHERE id = (?)";
        return jdbcTemplate.queryForObject(query, new Object[]{id}, (rs, rowNum) -> {
            Post post = new Post(
                    rs.getString("author"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rs.getTimestamp("created")),
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

    public List<Pair<Integer, Integer[]>> getChildren(Integer thread){
        String query = "SELECT id, array_for_tree FROM posts WHERE thread = (?)";
        return jdbcTemplate.query(query, (rs, rowNum) -> new Pair(rs.getInt("id"), (Integer[])rs.getArray("array_for_tree").getArray()), thread);
    }

    public List<Post> addPosts(List<Post> posts, ForumThread thread, List<Integer[]> paths) throws SQLException {
        String query = "INSERT INTO posts (author, created, forum, message, parent, thread, id, array_for_tree)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, array_append(?, ?))";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        try(Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.NO_GENERATED_KEYS);
            PreparedStatement forumUsersStatement = connection.prepareStatement(
                    "INSERT INTO users_in_forum (user_, forum_slug) " +
                            "VALUES  (?, ?)", Statement.NO_GENERATED_KEYS);
            List<Integer> listOfId = jdbcTemplate.queryForList("SELECT nextval('posts_id_seq') from generate_series(1, ?)", Integer.class, posts.size());
            int i = 0;
            int currentId;
            for (Post post : posts) {
                if(paths.get(i) == null){
                    preparedStatement.setArray(8, null);
                }
                else{
                    preparedStatement.setArray(8, connection.createArrayOf("int4", paths.get(i)));
                }
                currentId = listOfId.get(i++);
                post.setId(currentId);
                post.setCreated(dateFormat.format(now));
                post.setForum(thread.getForum());
                post.setThread(thread.getId());
                preparedStatement.setString(1, post.getAuthor());
                preparedStatement.setTimestamp(2, now);
                preparedStatement.setString(3, thread.getForum());
                preparedStatement.setString(4, post.getMessage());
                preparedStatement.setInt(5, post.getParent());
                preparedStatement.setInt(6, post.getThread());
                preparedStatement.setInt(7, currentId);
                preparedStatement.setInt(9, currentId);

                forumUsersStatement.setString(1, post.getAuthor());
                forumUsersStatement.setString(2, post.getForum());
                forumUsersStatement.addBatch();
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            forumUsersStatement.executeBatch();
            forumUsersStatement.close();
            preparedStatement.close();

        }
        catch(SQLException e){return null;}
        jdbcTemplate.update("UPDATE forums SET posts = posts + (?) WHERE LOWER(slug) = LOWER(?)", posts.size(), thread.getForum());
        return posts;
    }

    public void changePost(Post post, int id){
        String query = "UPDATE posts" +
                " SET message = COALESCE(?, message), isEdited = TRUE " +
                "WHERE id = ?";
        jdbcTemplate.update(query, new Object[]{post.getMessage(), id});
    }

    public List<Post> getFlatPosts(Integer threadId, int limit, int offset, Boolean desc){
        String query = "SELECT * FROM posts" +
                " WHERE thread = (?) " +
                " ORDER BY posts.created " + (desc == Boolean.TRUE ? "DESC" : "") + ", posts.id " +
                (desc == Boolean.TRUE ? "DESC" : "") + " LIMIT ? OFFSET ?";
        return jdbcTemplate.query(query,
                (rs, rowNum) -> {
                    Post post = new Post(
                            rs.getString("author"),
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rs.getTimestamp("created")),
                            rs.getString("forum"),
                            rs.getInt("id"),
                            rs.getBoolean("isEdited"),
                            rs.getString("message"),
                            rs.getInt("parent"),
                            rs.getInt("thread")
                    );
                    return post;
                },
                threadId,
                limit,
                offset);
    }

    public List<Post> getTreePosts(Integer threadId, int limit, int offset, Boolean desc){

        String query = "SELECT * " +
                " FROM posts " +
                " WHERE thread = (?) " +
                " ORDER BY array_for_tree " + (desc == Boolean.TRUE ? "DESC" : "") +
                " LIMIT ? OFFSET ?";
        return jdbcTemplate.query(query, (rs, rowNum) -> {
            Post post = new Post(
                    rs.getString("author"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rs.getTimestamp("created")),
                    rs.getString("forum"),
                    rs.getInt("id"),
                    rs.getBoolean("isEdited"),
                    rs.getString("message"),
                    rs.getInt("parent"),
                    rs.getInt("thread")
            );
            return post;
                },
                threadId,
                limit,
                offset
                );
    }

    public List<Integer> getParents(Integer threadId, int limit, int offset, Boolean desc){
        String query =
                " SELECT id FROM posts " +
                        " WHERE parent = 0 AND thread = (?) " +
                        " ORDER BY id " + (desc == Boolean.TRUE ? "DESC " : "") + " LIMIT ? OFFSET ?;";
        return jdbcTemplate.query(query, parentMapper, threadId, limit, offset);
    }

    public List<Post> getParentTreePosts(Integer threadId, List<Integer> parents, Boolean desc){
        String query =
                "SELECT * " +
                "FROM posts " +
                "WHERE thread = (?) AND array_for_tree[1] = (?) " +
                "ORDER BY array_for_tree " + (desc == Boolean.TRUE ? "DESC" : "") + ", id " + (desc == Boolean.TRUE ? "DESC" : "");
        List<Post> result = new ArrayList<>();

        for (Integer parentId : parents){
            result.addAll(jdbcTemplate.query(
                    query,
                    (rs, rowNum) -> {
                        Post post = new Post(
                                rs.getString("author"),
                                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rs.getTimestamp("created")),
                                rs.getString("forum"),
                                rs.getInt("id"),
                                rs.getBoolean("isEdited"),
                                rs.getString("message"),
                                rs.getInt("parent"),
                                rs.getInt("thread")
                        );
                        return post;
                    },
                    threadId,
                    parentId
            ));
        }
        return result;
    }
}

