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
        String query = "INSERT INTO votes (?, ?, ?)" +
                "VALUES (nickname, voice, thread)";
        jdbcTemplate.update(query, vote.getNickname(), vote.getVoice(), threadId);

    }

    public void changeVote(Vote vote, int threadId){
        String query = "UPDATE votes SET voice = (?) " +
                "WHERE nickname = LOWER(?) AND thread = (?)";
        jdbcTemplate.update(query, vote.getVoice(), vote.getNickname(), threadId);
    }
}
