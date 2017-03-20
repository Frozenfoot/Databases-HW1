package DB.Controllers;

import DB.Models.*;
import DB.Services.PostsService;
import DB.Services.ThreadService;
import DB.Services.UserService;
import DB.Services.VoiceService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Created by frozenfoot on 15.03.17.
 */
@RestController
@RequestMapping("api/thread/{slug_or_id}")
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
            value = "/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addPosts(
            @PathVariable("slug_or_id") String slug,
            @RequestBody List<Post> posts
    ) {
        Integer id = null;
        ForumThread thread = null;
        Post parent;
        User author;
        try{
            id = Integer.parseInt(slug);
        }
        catch (NumberFormatException e){
            id = null;
        }
        try{
            if(id == null){
                thread = threadService.getThread(slug);
            }
            else{
                thread = threadService.getThread(id);
            }
        }
        catch (EmptyResultDataAccessException e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        for(Post post : posts){

            if(id != null){
                post.setThread(id);
            }
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
        List<Post> result = postsService.getLastPosts(posts.size(), thread);
        List<?> shallowCopy = result.subList(0, result.size());
        Collections.reverse(shallowCopy);

        return new ResponseEntity(result, HttpStatus.CREATED);
    }

    @RequestMapping(
            value = "/details",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getThreadDetails(@PathVariable("slug_or_id") String slug){
        Integer id;
        ForumThread thread;
        try{
            id = Integer.parseInt(slug);
        }
        catch (NumberFormatException e){
            id = null;
        }
        try{
            if(id == null) {
                thread = threadService.getThread(slug);
            }
            else{
                thread = threadService.getThread(id);
            }
        }
        catch (EmptyResultDataAccessException e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
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
        Integer id;
        ForumThread dbThread;
        try{
            id = Integer.parseInt(slug);
        }
        catch (NumberFormatException e){
            id = null;
        }

        try{
            if(id == null){
                dbThread = threadService.getThread(slug);
            }
            else {
                dbThread = threadService.getThread(id);
            }
        }
        catch (EmptyResultDataAccessException e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
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
            @RequestParam(value = "marker", required = false, defaultValue = "0") String marker,
            @RequestParam(value = "sort", required = false, defaultValue = "flat") String sort,
            @RequestParam(value = "desc", required = false) Boolean desc
    ){
        ForumThread thread;
        List<Post> posts = null;

        Integer offset = 0;
        Integer id = null;
        if(marker.matches("\\d+")){
            offset = Integer.parseInt(marker);
        }
        else{
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        try {
             id = Integer.parseInt(slug);
        }
        catch (NumberFormatException e){
            id = null;
        }

        try{
            if (id == null){
                thread = threadService.getThread(slug);
            }
            else{
                thread = threadService.getThread(id);
            }
        }
        catch (EmptyResultDataAccessException e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        int sizeOfOffset = 0;
        switch (sort){
            case "flat":
                posts = postsService.getFlatPosts(thread.getSlug(), limit, offset, desc);
                sizeOfOffset = posts.size();
                break;
            case "tree":
                posts = postsService.getTreePosts(thread.getSlug(), limit, offset, desc);
                sizeOfOffset = posts.size();
                break;
            case "parent_tree":
                List<Integer> parents = postsService.getParents(thread.getSlug(), limit, offset, desc);
                posts = postsService.getParentTreePosts(thread.getSlug(), parents, desc);
                sizeOfOffset = parents.size();
                break;
        }
        offset += sizeOfOffset;
        return new ResponseEntity(new ThreadPosts(offset.toString(), posts), HttpStatus.OK);
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
        Integer id;
        ForumThread dbThread;
        try{
            id = Integer.parseInt(slug);
        }
        catch (NumberFormatException e){
            id = null;
        }

        try{
            if(id == null){
                dbThread = threadService.getThread(slug);
            }
            else{
                dbThread = threadService.getThread(id);
            }
        }
        catch (EmptyResultDataAccessException e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        try{
            User voteUser = userService.getUser(vote.getNickname());
        }
        catch (EmptyResultDataAccessException e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        try {
            int threadId = voiceService.getVote(vote.getNickname(), dbThread.getId());
            voiceService.changeVote(vote, threadId);
        }
        catch (EmptyResultDataAccessException e){
            voiceService.addVote(vote, dbThread.getId());
        }
        return new ResponseEntity(threadService.getThread(dbThread.getId()), HttpStatus.OK);
    }
}
