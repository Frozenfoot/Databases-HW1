package DB.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by frozenfoot on 15.03.17.
 */
public class Service {
    private int forum;
    private int post;
    private int thread;
    private int user;

    @JsonCreator
    public Service(
            @JsonProperty("forum") int forum,
            @JsonProperty("fpost") int post,
            @JsonProperty("thread") int thread,
            @JsonProperty("user") int user
    ) {
        this.forum = forum;
        this.post = post;
        this.thread = thread;
        this.user = user;
    }

    public int getForum() {
        return forum;
    }

    public void setForum(int forum) {
        this.forum = forum;
    }

    public int getPost() {
        return post;
    }

    public void setPost(int post) {
        this.post = post;
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }
}
