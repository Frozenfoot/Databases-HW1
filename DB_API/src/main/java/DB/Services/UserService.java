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
                "SELECT DISTINCT u.nickname COLLATE \"ucs_basic\", u.fullname, u.email, u.about, LOWER (nickname COLLATE \"ucs_basic\") AS lowNickname" +
                        " FROM users u " +
                "LEFT JOIN threads t ON (u.nickname = t.author) " +
                "LEFT JOIN posts p ON (u.nickname = p.author) " +
                "JOIN forums f ON (LOWER(f.slug)=LOWER(?) AND (f.slug = t.forum OR f.slug = p.forum)) ");
        ArrayList<Object> arguments = new ArrayList<>();
        arguments.add(slug);

        if(since != null) {
            if (desc == Boolean.TRUE) {
                query.append(" WHERE LOWER(nickname COLLATE \"ucs_basic\") < LOWER(? COLLATE \"ucs_basic\") ");
            } else {
                query.append(" WHERE LOWER(nickname COLLATE \"ucs_basic\") > LOWER(? COLLATE \"ucs_basic\") ");
            }
            arguments.add(since);
        }
//        query.append(" GROUP BY nickname ");
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
