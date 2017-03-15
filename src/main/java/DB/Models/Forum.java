package DB.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by frozenfoot on 15.03.17.
 */
public class Forum {
    private int posts;
    private String slug;
    private int threads;
    private String title;
    private String user;

    @JsonCreator
    public Forum(
            @JsonProperty("posts") final int posts,
            @JsonProperty("slug") final String slug,
            @JsonProperty("threads") final int threads,
            @JsonProperty("title") String title,
            @JsonProperty("user") String user
    ){
        this.posts = posts;
        this.slug = slug;
        this.threads = threads;
        this.title = title;
        this.user = user;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getPosts() {

        return posts;
    }

    public String getSlug() {
        return slug;
    }

    public int getThreads() {
        return threads;
    }

    public String getTitle() {
        return title;
    }

    public String getUser() {
        return user;
    }
}
