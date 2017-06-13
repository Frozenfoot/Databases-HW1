package DB.Services;

import DB.Models.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frozenfoot on 15.03.17.
 */
@Service
public class UserService {

    private JdbcTemplate jdbcTemplate;
    public UserService(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addUser(User user){
        String querry = "INSERT INTO users (about, email, fullname, nickname)" +
                "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(querry, user.getAbout(), user.getEmail(), user.getFullname(), user.getNickname());
    }

    public User getUser(String nickname){

        String querry = "SELECT * FROM users WHERE LOWER(users.nickname) = LOWER(?)";
            return jdbcTemplate.queryForObject(querry, new Object[]{nickname},
                    (rs, rowNum) -> new User(
                            rs.getString("about"),
                            rs.getString("email"),
                            rs.getString("fullname"),
                            rs.getString("nickname")
                    ));
    }

    public List<User> getUser(User user){
        String querry = "SELECT * FROM users WHERE LOWER(users.nickname) = LOWER(?)" +
                "OR LOWER(users.email) = LOWER(?)";
        return jdbcTemplate.query(querry,
                new Object[]{user.getNickname(), user.getEmail()},
                (rs, rowNum) -> new User(
                        rs.getString("about"),
                        rs.getString("email"),
                        rs.getString("fullname"),
                        rs.getString("nickname")
                ));
    }

    public void updateUser(User user){
        String querry = "UPDATE users " +
                "SET about = COALESCE(?, about)," +
                " email = COALESCE(?, email)," +
                " fullname = COALESCE(?, fullname)" +
                "WHERE LOWER (nickname) = LOWER(?)";
        jdbcTemplate.update(querry, new Object[]{user.getAbout(),
                user.getEmail(),
                user.getFullname(),
                user.getNickname()});
    }

    public List<User> getForumUsers(String slug, int limit, String since, Boolean desc){

        StringBuilder query = new StringBuilder(
                "SELECT * " +
                        "FROM users " +
                        "WHERE nickname IN (" +
                        "   SELECT user_ " +
                        "   FROM users_in_forum " +
                        "   WHERE LOWER (forum_slug) = LOWER(?)) "
        );
        ArrayList<Object> arguments = new ArrayList<>();
        arguments.add(slug);

        if(since != null) {
            if (desc == Boolean.TRUE) {
                query.append(" AND LOWER(nickname COLLATE \"ucs_basic\") < LOWER(? COLLATE \"ucs_basic\") ");
            } else {
                query.append(" AND LOWER(nickname COLLATE \"ucs_basic\") > LOWER(? COLLATE \"ucs_basic\") ");
            }
            arguments.add(since);
        }
        query.append("ORDER BY LOWER(nickname COLLATE \"ucs_basic\") "
                + (desc == Boolean.TRUE ? "DESC " : ""));
        query.append(" LIMIT ?");
        arguments.add(limit);
        return jdbcTemplate.query(query.toString(), arguments.toArray(), (rs, rowNum) -> {
            User user = new User(
                    rs.getString("about"),
                    rs.getString("email"),
                    rs.getString("fullname"),
                    rs.getString("nickname")
            );
            return user;
        });
    }
}
