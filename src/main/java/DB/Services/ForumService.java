package DB.Services;

import DB.Models.Forum;
import DB.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Created by frozenfoot on 15.03.17.
 */
@Service
public class ForumService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ForumService(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addForum(Forum forum){

        String querry = "INSERT INTO forums (slug, title, user_)" +
                "VALUES(?, ?, (SELECT nickname FROM users WHERE LOWER(nickname) = LOWER(?)))";
        jdbcTemplate.update(querry, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public Forum getForum(String slug){

        String querry = "SELECT COUNT(*) " +
                " FROM threads " +
                " WHERE LOWER(forum) = LOWER(?)";
        int threads = jdbcTemplate.queryForObject(querry, Integer.class, slug);
        System.out.println("Threads: " + threads);

        querry = "SELECT COUNT(*) " +
                " FROM posts " +
                " WHERE LOWER(forum) = LOWER(?)";
        int posts = jdbcTemplate.queryForObject(querry, Integer.class, slug);

        querry = "SELECT *" +
                " FROM forums " +
                " WHERE LOWER(forums.slug) = LOWER(?)";
        return jdbcTemplate.queryForObject(
                querry,
                (rs, rowNum) -> new Forum(
                        posts,
                        rs.getString("slug"),
                        threads,
                        rs.getString("title"),
                        rs.getString("user_")
                ),
                slug);
    }
}
