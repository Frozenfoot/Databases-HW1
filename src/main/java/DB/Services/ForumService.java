package DB.Services;

import DB.Models.Forum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
                "VALUES(?, ?, ?)";
        jdbcTemplate.update(querry, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public Forum getForum(String slug){

        String querry = "SELECT posts, slug, title, user_, COUNT(t) AS threadsCount" +
                " FROM forums" +
                " JOIN threads t ON forums.slug = threads.forum" +
                " WHERE forums.slug = LOWER(?)" +
                " GROUP BY posts, slug, title, user_";
        return jdbcTemplate.queryForObject(querry, new Object[]{slug}, (rs, rowNum) -> {
            Forum forum = new Forum(
                    rs.getInt("posts"),
                    rs.getString("slug"),
                    rs.getInt("threadsCount"),
                    rs.getString("title"),
                    rs.getString("user_")
            );
            return forum;
        });
    }
}
