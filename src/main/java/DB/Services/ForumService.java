package DB.Services;

import DB.Models.Forum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import javax.xml.ws.ServiceMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by frozenfoot on 15.03.17.
 */
@Service
public class ForumService {
    private JdbcTemplate jdbcTemplate;

    public ForumService(){
        jdbcTemplate = new JdbcTemplate();
    }

    public void addForum(Forum forum){
        Map<String, Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("slug", forum.getSlug());
        parametersMap.put("title", forum.getTitle());
        parametersMap.put("user", forum.getUser());

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("slug", forum.getSlug())
                .addValue("title", forum.getTitle())
                .addValue("user", forum.getUser());
        String querry = "INSERT INTO forums" +
                "(slug, title, \"user\")" +
                "VALUES( :slug, :title, :user)";
        jdbcTemplate.update(querry, parameters);
    }
}
