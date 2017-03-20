package DB.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by frozenfoot on 17.03.17.
 */
@Service
public class ServiceService {
    @Autowired
    JdbcTemplate jdbcTemplate;
    public ServiceService(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public DB.Models.Service getServiceStatus(){
        return new DB.Models.Service(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM forums", Integer.class),
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts", Integer.class),
                jdbcTemplate.queryForObject("SELECT COUNT (*) FROM threads", Integer.class),
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class)
        );
    }

    public void clearService(){
        jdbcTemplate.execute("TRUNCATE forums, posts, threads, users, votes");
    }
}
