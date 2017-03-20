package DB.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by frozenfoot on 19.03.17.
 */
public class ThreadPosts {
    @JsonProperty
    private String marker;

    @JsonProperty
    private List<Post> posts;

    @JsonCreator
    public ThreadPosts(@JsonProperty("marker") String marker,
                       @JsonProperty("posts") List<Post> posts){
        this.marker = marker;
        this.posts = posts;
    }

    public ThreadPosts(){};

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}
