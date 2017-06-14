package DB.Services;

import DB.Models.Forum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by frozenfoot on 15.03.17.
 */
@Service
@Transactional
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
        String query = "SELECT *" +
                " FROM forums " +
                " WHERE LOWER(forums.slug) = LOWER(?)";
        return jdbcTemplate.queryForObject(
                query,
                (rs, rowNum) -> new Forum(
                        rs.getInt("posts"),
                        rs.getString("slug"),
                        rs.getInt("threads"),
                        rs.getString("title"),
                        rs.getString("user_")
                ),
                slug);
    }
}
