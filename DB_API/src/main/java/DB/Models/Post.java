package DB.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by frozenfoot on 15.03.17.
 */

public class Post {
    private String author;
    private String created;
    private String forum;
    private int id;
    private Boolean isEdited;
    private String message;
    private int parent;
    private int thread;

    @JsonCreator
    public Post(
            @JsonProperty("author") String author,
            @JsonProperty("created") String created,
            @JsonProperty("forum") String forum,
            @JsonProperty("id") int id,
            @JsonProperty("isEdited") Boolean isEdited,
            @JsonProperty("message") String message,
            @JsonProperty("parent") int parent,
            @JsonProperty("thread") int thread
    ) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.isEdited = isEdited;
        this.message = message;
        this.parent = new Integer(parent) == null ? 0 : parent;
        this.thread = thread;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }
}
