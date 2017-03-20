package DB.Services;

import DB.Models.ForumThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
public class ThreadService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    public ThreadService(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addThread(ForumThread thread){


        String querry = "SELECT slug FROM forums WHERE slug = LOWER(?)";

        if(thread.getCreated() == null){
            thread.setCreated(LocalDateTime.now().toString());
        }

        Timestamp createTime = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME));

        if(!thread.getCreated().endsWith("Z")){
            createTime = Timestamp.from(createTime.toInstant().plusSeconds(-10800));
        }

        querry = "INSERT INTO threads (author, created, forum, message, slug, title)" +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        return jdbcTemplate.queryForObject(querry,
                Integer.class,
                thread.getAuthor(),
                createTime,
                thread.getForum(),
                thread.getMessage(),
                thread.getSlug(),
                thread.getTitle()
        );
    }

    public ForumThread getThread(String slug){
        String query = "SELECT SUM(voice) " +
                " FROM votes" +
                " WHERE thread = (" +
                " SELECT id FROM threads " +
                " WHERE LOWER(threads.slug) = LOWER(?))";
        Integer votes = jdbcTemplate.queryForObject(query, Integer.class, slug);

        query = "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)";
        return jdbcTemplate.queryForObject(query, new Object[]{slug}, (rs, rowNum) -> {
            ForumThread thread = new ForumThread(
                    rs.getString("author"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rs.getTimestamp("created")),
                    rs.getString("forum"),
                    rs.getInt("id"),
                    rs.getString("message"),
                    rs.getString("slug"),
                    rs.getString("title"),
                    (votes == null ? 0 : votes.intValue())
            );
            return thread;
        });
    }

    public ForumThread getThread(int id){
        String query = "SELECT SUM(voice) " +
                " FROM votes" +
                " WHERE votes.thread = ? ";
        Integer votes = jdbcTemplate.queryForObject(query, Integer.class, id);

        query = "SELECT * FROM threads WHERE id = (?)";
        return jdbcTemplate.queryForObject(query, new Object[]{id}, (rs, rowNum) -> {
            ForumThread thread = new ForumThread(
                    rs.getString("author"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rs.getTimestamp("created")),
                    rs.getString("forum"),
                    rs.getInt("id"),
                    rs.getString("message"),
                    rs.getString("slug"),
                    rs.getString("title"),
                    (votes == null ? 0 : votes.intValue())
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

//        for (ForumThread thread : result ){
//
//        }
        return result;
    }

    public void changeThread(ForumThread thread, int id){
        String query = "UPDATE threads SET message = (?), title = (?) WHERE id = (?)";
        jdbcTemplate.update(query, thread.getMessage(), thread.getTitle(), id);
    }
}
