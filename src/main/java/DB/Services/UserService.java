package DB.Services;

import DB.Models.ForumThread;
import DB.Models.User;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        String querry = "SELECT * FROM users WHERE LOWER(users.nickname) = LOWER(?)" +
                " OR LOWER(users.email) = LOWER(?)";
            return jdbcTemplate.queryForObject(querry, new Object[]{nickname, nickname},
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
        String querry = "UPDATE users SET about = (?), email = (?), fullname = (?)" +
                "WHERE LOWER (nickname) = LOWER(?)";
        jdbcTemplate.update(querry, new Object[]{user.getAbout(),
                user.getEmail(),
                user.getFullname(),
                user.getNickname()});
    }

    public List<User> getForumUsers(String slug, int limit, String since, Boolean desc){
        ArrayList<Object> arguments = new ArrayList<>();
        arguments.add(slug);

        StringBuilder query = new StringBuilder("SELECT DISTINCT *" +
                " FROM users" +
                " LEFT JOIN threads ON threads.author = users.id" +
                " LEFT JOIN posts ON posts.author = users.nickname" +
                " JOIN forums ON forums.slug = LOWER (?) AND " +
                "(forums.slug = threads.forum OR forums.slug = posts.forum)");
        if(since != null) {
            query.append(" WHERE nickname ");
            if (desc) {
                query.append("< ");
            } else {
                query.append("> ");
            }
            arguments.add(since);
            query.append(" (?)");
        }
        query.append(" ORDER BY nickname " + (desc ? "DESC " : ""));
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
