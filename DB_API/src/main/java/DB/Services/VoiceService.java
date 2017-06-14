package DB.Services;

import DB.Models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by frozenfoot on 18.03.17.
 */
@Service
@Transactional
public class VoiceService {
    @Autowired
    JdbcTemplate jdbcTemplate;
    public VoiceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addVote(Vote vote, int threadId){

        String query = "SELECT COUNT(*) FROM votes WHERE LOWER(nickname) = LOWER(?) AND thread = (?)";
        Integer inserted = jdbcTemplate.queryForObject(query, Integer.class, vote.getNickname(), threadId);
        if(inserted == 0){
            query =
                    "INSERT INTO votes (nickname, voice, thread) " +
                            "VALUES (?, ?, ?) ";
            jdbcTemplate.update(query, vote.getNickname(), vote.getVoice(), threadId);
        }
        else{
            query = "UPDATE votes SET voice = (?) WHERE LOWER(nickname) = LOWER(?) AND thread = (?)";
            jdbcTemplate.update(query, vote.getVoice(), vote.getNickname(), threadId);
        }
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
