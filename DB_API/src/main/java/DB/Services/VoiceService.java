package DB.Services;

import DB.Models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by frozenfoot on 18.03.17.
 */
@Service
public class VoiceService {
    @Autowired
    JdbcTemplate jdbcTemplate;
    public VoiceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addVote(Vote vote, int threadId){
        String query = "INSERT INTO votes (nickname, voice, thread)" +
                "VALUES (?, ?, ?)";
        jdbcTemplate.update(query, vote.getNickname(), vote.getVoice(), threadId);

    }

    public void changeVote(Vote vote, int id){
        String query = "UPDATE votes SET voice = (?) " +
                "WHERE id =  (?)";
        jdbcTemplate.update(query, vote.getVoice(), id);
    }

    public int getVote(String nickname, int threadId){
        String query = "SELECT id " +
                "FROM votes " +
                "WHERE LOWER(nickname) = LOWER(?) AND thread = (?)";
        return jdbcTemplate.queryForObject(query, Integer.class, nickname, threadId);
    }
}
