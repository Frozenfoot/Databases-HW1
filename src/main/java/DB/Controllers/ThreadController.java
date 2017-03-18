package DB.Controllers;

import DB.Models.*;
import DB.Services.PostsService;
import DB.Services.ThreadService;
import DB.Services.UserService;
import DB.Services.VoiceService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by frozenfoot on 15.03.17.
 */
@RestController
@RequestMapping("thread/{slug_or_id}")
public class ThreadController {

    private JdbcTemplate jdbcTemplate;
    private ThreadService threadService;
    private PostsService postsService;
    private UserService userService;
    private VoiceService voiceService;

    public ThreadController(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
        threadService = new ThreadService(jdbcTemplate);
        postsService = new PostsService(jdbcTemplate);
        userService = new UserService(jdbcTemplate);
        voiceService = new VoiceService(jdbcTemplate);
    }

    @RequestMapping(
            value = "{slug_or_id}/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addPosts(
            @PathVariable("slug_or_id") String slug,
            @RequestBody List<Post> posts
    ) {
        int id;
        ForumThread thread;
        Post parent;
        User author;
        try{
            thread = threadService.getThread(slug);
        }
        catch (EmptyResultDataAccessException e){
            id = Integer.parseInt(slug);
            try{
                thread = threadService.getThread(id);
            }
            catch (EmptyResultDataAccessException e1){
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }

        for(Post post : posts){

            if(post.getParent() != 0){
                try{
                    parent = postsService.getPost(post.getParent());
                }
                catch (EmptyResultDataAccessException e){
                    return new ResponseEntity(HttpStatus.CONFLICT);
                }
                if(parent.getThread() != thread.getId()){
                    return new ResponseEntity(HttpStatus.CONFLICT);
                }
            }

            try{
                author = userService.getUser(post.getAuthor());
            }
            catch (EmptyResultDataAccessException e){
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }

            if(post.getCreated() == null){
                post.setCreated(LocalDateTime.now().toString());
            }
            postsService.addPost(post, thread);
        }
        return new ResponseEntity(postsService.getLastPosts(posts.size(), thread), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/details",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getThreadDetails(@PathVariable("slug_or_id") String slug){
        int id;
        ForumThread thread;
        try{
            thread = threadService.getThread(slug);
        }
        catch (EmptyResultDataAccessException e){
            id = Integer.parseInt(slug);
            try{
                thread = threadService.getThread(id);
            }
            catch (EmptyResultDataAccessException e1){
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity(thread, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/details",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity changeThreadDetails(
            @PathVariable("slug_or_id") String slug,
            @RequestBody ForumThread thread
    ) {
        int id;
        ForumThread dbThread;
        try{
            dbThread = threadService.getThread(slug);
        }
        catch (EmptyResultDataAccessException e){
            id = Integer.parseInt(slug);
            try{
                dbThread = threadService.getThread(id);
            }
            catch (EmptyResultDataAccessException e1){
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }
        threadService.changeThread(thread, dbThread.getId());
        return new ResponseEntity(threadService.getThread(dbThread.getId()), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/posts",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getPostsInThread(
            @PathVariable("slug_or_id") String slug,
            @RequestParam(value = "limit", required = false, defaultValue = "100"                                                                                                                                                       ) int limit,
            @RequestParam(value = "marker", required = false) String marker,
            @RequestParam(value = "sort", required = false, defaultValue = "flat") String sort,
            @RequestParam(value = "desc", required = false) Boolean desc
    ) throws JSONException {
        ForumThread thread;
        JSONObject result = new JSONObject();
        List<Post> posts = null;
        try {
             thread = threadService.getThread(slug);
        }
        catch (EmptyResultDataAccessException e){
            try {
                thread = threadService.getThread(Integer.parseInt(slug));
            }
            catch (EmptyResultDataAccessException e1){
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }
        switch (sort){
            case "flat":
                posts = postsService.getFlatPosts(thread.getSlug(), limit, Integer.parseInt(marker), desc);
                break;
            case "tree":
                posts = postsService.getTreePosts(thread.getSlug(), limit, Integer.parseInt(marker), desc);
                break;
            case "parent_tree":
                List<Integer> parents = postsService.getParents(thread.getSlug(), limit, Integer.parseInt(marker), desc);
                posts = postsService.getParentTreePosts(thread.getSlug(), parents, desc);
                break;
        }
        result.put("marker", marker);
        result.put("posts", posts);

        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/vote",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity voteForThread(
            @PathVariable("slug_or_id") String slug,
            @RequestBody Vote vote
    ) {
        int id;
        ForumThread dbThread;
        try{
            dbThread = threadService.getThread(slug);
        }
        catch (EmptyResultDataAccessException e){
            id = Integer.parseInt(slug);
            try{
                dbThread = threadService.getThread(id);
            }
            catch (EmptyResultDataAccessException e1){
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }

        try {
            voiceService.addVote(vote, dbThread.getId());
        }
        catch (DuplicateKeyException e){
            voiceService.changeVote(vote, dbThread.getId());
        }
        return new ResponseEntity(threadService.getThread(dbThread.getId()), HttpStatus.OK);
    }
}
