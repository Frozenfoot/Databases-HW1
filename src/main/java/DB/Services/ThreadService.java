package DB.Services;

import DB.Models.ForumThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by frozenfoot on 17.03.17.
 */
@Service
public class ThreadService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    public ThreadService(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addThread(ForumThread thread){

        String querry = "SELECT slug FROM forums WHERE slug = LOWER(?)";

        if(thread.getCreated() == null){
            thread.setCreated(LocalDateTime.now().toString());
        }

        String forumSlug = jdbcTemplate.queryForObject(querry, new Object[]{thread.getForum()}, String.class);
        querry = "INSERT INTO threads (author, created, forum, message, slug, title)" +
                "VALUES (?, ?, ?, ?, ?, ?)";
        if(thread.getCreated() == null){
            thread.setCreated(LocalDateTime.now().toString());
        }

        jdbcTemplate.update(querry,
                thread.getAuthor(),
                Timestamp.valueOf(LocalDateTime.parse(thread.getCreated())),
                forumSlug,
                thread.getMessage(),
                thread.getSlug(),
                thread.getTitle()
        );
    }

    public ForumThread getThread(String slug){
        String query = "SELECT * FROM threads WHERE slug = LOWER(?)";
        return jdbcTemplate.queryForObject(query, new Object[]{slug}, (rs, rowNum) -> {
            ForumThread thread = new ForumThread(
                    rs.getString("author"),
                    rs.getString("created"),
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
        String query = "SELECT COUNT(*) FROM votes" +
                "WHERE thread = ?";
        int voices = jdbcTemplate.queryForObject(query, Integer.class);
        query = "SELECT author, created, forum, id, message, slug, title, SUM(v.voice) AS votes " +
                "FROM threads " +
                "JOIN votes v ON v.thread = threads.id " +
                "WHERE threads.id = ? " +
                "GROUP BY author, created, forum, id, message, slug, title";
        return jdbcTemplate.queryForObject(query, new Object[]{id}, (rs, rowNum) -> {
            ForumThread thread = new ForumThread(
                    rs.getString("author"),
                    rs.getString("created"),
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
        query.append("SELECT * FROM threads WHERE threads.forum = LOWER(?)");
        parameters.add(slug);

        if(since != null){
            if(desc != null && desc) {
                query.append(" AND threads.created <= (?)");
            }
            else{
                query.append(" AND threads.created >= (?)");
            }
            parameters.add(since);
        }
        query.append(" ORDER BY created");

        if(desc != null && desc){
            query.append(" DESC");
        }

        query.append(" LIMIT (?)");
        parameters.add(limit);
        return jdbcTemplate.query(query.toString(), parameters.toArray(), (rs, rowNum) -> {
            ForumThread thread = new ForumThread(
                    rs.getString("author"),
                    rs.getString("created"),
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

    public void changeThread(ForumThread thread, int id){
        String query = "UPDATE threads SET message = (?), title = (?) WHERE id = (?)";
        jdbcTemplate.update(query, thread.getMessage(), thread.getTitle(), id);
    }
}
