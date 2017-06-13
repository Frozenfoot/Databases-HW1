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
        String query =
                "INSERT INTO votes (nickname, voice, thread) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (nickname, thread) DO UPDATE SET voice = EXCLUDED.voice";
        jdbcTemplate.update(query, vote.getNickname(), vote.getVoice(), threadId);
        query =
                "UPDATE threads " +
                "SET votes = (" +
                        "SELECT SUM(voice) " +
                        "FROM votes " +
                        "WHERE (thread) = (?)" +
                        ")" +
                        "WHERE id = (?)";
        jdbcTemplate.update(query, threadId, threadId);
    }
}
