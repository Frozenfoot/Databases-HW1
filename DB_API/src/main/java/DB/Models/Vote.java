package DB.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by frozenfoot on 15.03.17.
 */
public class Vote {
    private String nickname;
    private int voice;

    @JsonCreator
    public Vote(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("voice") int voice
    ) {
        this.nickname = nickname;
        this.voice = voice;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getVoice() {
        return voice;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }
}
