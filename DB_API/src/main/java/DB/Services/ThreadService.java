package DB.Services;

import DB.Models.ForumThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by frozenfoot on 17.03.17.
 */
@Service
@Transactional
public class ThreadService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    public ThreadService(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addThread(ForumThread thread){
        if(thread.getCreated() == null){
            thread.setCreated(LocalDateTime.now().toString());
        }

        Timestamp createTime = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME));

        if(!thread.getCreated().endsWith("Z")){
            createTime = Timestamp.from(createTime.toInstant().plusSeconds(-10800));
        }

        String query = "INSERT INTO threads (author, created, forum, message, slug, title)" +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        int id =  jdbcTemplate.queryForObject(query,
                Integer.class,
                thread.getAuthor(),
                createTime,
                thread.getForum(),
                thread.getMessage(),
                thread.getSlug(),
                thread.getTitle()
        );
        jdbcTemplate.update(
                "UPDATE forums SET threads = threads + 1 WHERE LOWER(slug) = LOWER(?)", thread.getForum()
        );

        jdbcTemplate.update("INSERT INTO users_in_forum (user_, forum_slug) VALUES ((?), (?))", thread.getAuthor(), thread.getForum());
        return id;
    }

    public ForumThread getThread(String slug){
        String query = "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)";
        return jdbcTemplate.queryForObject(query, new Object[]{slug}, (rs, rowNum) -> {
            ForumThread thread = new ForumThread(
                    rs.getString("author"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rs.getTimestamp("created")),
                    rs.getString("forum"),
                    rs.getInt("id"),
                    rs.getString("message"),
                    rs.getString("slug"),
                    rs.getString("title"),
                    rs.getInt("votes")
            );
            return thread;
        });
    }

    public ForumThread getThread(int id){
        String query = "SELECT * FROM threads WHERE id = (?)";
        return jdbcTemplate.queryForObject(query, new Object[]{id}, (rs, rowNum) -> {
            ForumThread thread = new ForumThread(
                    rs.getString("author"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rs.getTimestamp("created")),
                    rs.getString("forum"),
                    rs.getInt("id"),
                    rs.getString("message"),
                    rs.getString("slug"),
                    rs.getString("title"),
                    rs.getInt("votes")
            );
            return thread;
        });
    }

    public List<ForumThread> getThreads(String slug, int limit, String since, Boolean desc){
        ArrayList<Object> parameters = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM threads WHERE LOWER(threads.forum) = LOWER(?)");
        parameters.add(slug);

        if(since != null){
            if(desc == Boolean.TRUE) {
                query.append(" AND threads.created <= (?)");
            }
            else{
                query.append(" AND threads.created >= (?)");
            }
            parameters.add(Timestamp.valueOf(LocalDateTime.parse(since, DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        }
        query.append(" ORDER BY created");

        if(desc == Boolean.TRUE){
            query.append(" DESC");
        }

        query.append(" LIMIT (?)");
        parameters.add(limit);
        List<ForumThread> result = jdbcTemplate.query(query.toString(), parameters.toArray(), (rs, rowNum) -> {
            ForumThread thread = new ForumThread(
                    rs.getString("author"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rs.getTimestamp("created")),
                    rs.getString("forum"),
                    rs.getInt("id"),
                    rs.getString("message"),
                    rs.getString("slug"),
                    rs.getString("title"),
                    0
            );
            return thread;
        });
        return result;
    }

    public void changeThread(ForumThread thread, int id){
        String query = "UPDATE threads " +
                "SET message = COALESCE(?, message), " +
                "title = COALESCE(?, title) WHERE id = (?)";
        jdbcTemplate.update(query, thread.getMessage(), thread.getTitle(), id);
    }
}
