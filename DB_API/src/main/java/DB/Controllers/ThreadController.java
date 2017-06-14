package DB.Controllers;

import DB.Models.*;
import DB.Services.PostsService;
import DB.Services.ThreadService;
import DB.Services.UserService;
import DB.Services.VoiceService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by frozenfoot on 15.03.17.
 */
@SuppressWarnings("ALL")
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
        System.out.println("api/thread/create");
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

        List<Pair<Integer, Integer[]>> children = postsService.getChildren(thread.getId());
        List<Integer[]> paths = new ArrayList<>();

        for(Post post : posts){
            int parentID = post.getParent();
            if(parentID != 0){
                final Optional<Pair<Integer, Integer[]>> optional = children.stream().filter(e -> e.x == parentID).findFirst();
                if(!optional.isPresent()){
                    return new ResponseEntity(HttpStatus.CONFLICT);
                }
                else {
                    paths.add(optional.get().y);
                }
            }
            else {
                paths.add(null);
            }

            try{
                author = userService.getUser(post.getAuthor());
            }
            catch (EmptyResultDataAccessException e){
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }

        try{
            postsService.addPosts(posts, thread, paths);
        }
        catch (DuplicateKeyException e){
            return new ResponseEntity(HttpStatus.CONFLICT);
        } catch (SQLException e) {
        }

        return new ResponseEntity(posts, HttpStatus.CREATED);
    }

    @RequestMapping(
            value = "/details",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getThreadDetails(@PathVariable("slug_or_id") String slug){
        System.out.println("api/thread/details");
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
        catch (EmptyResultDataAccessException e) {
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
        System.out.println("api/thread/details change");
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
            @PathVariable(name = "slug_or_id") String slug,
            @RequestParam(name = "limit", required = false, defaultValue = "0") final Integer limit,
            @RequestParam(name = "marker", required = false, defaultValue = "0") final Integer marker,
            @RequestParam(name = "sort", required = false, defaultValue = "flat") final String sort,
            @RequestParam(name = "desc", required = false, defaultValue = "false") final Boolean desc
    )
    {
        System.out.println("api/thread/posts");
        ForumThread thread;

        Integer id = null;

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

        Integer offset = marker == null ? 0 : marker;
        List<Post> posts = new ArrayList<>();

        switch (sort){
            case "flat":
                posts = postsService.getFlatPosts(thread.getId(), limit, offset, desc);
                offset += posts.size();
                break;
            case "tree":
                posts = postsService.getTreePosts(thread.getId(), limit, offset, desc);
                offset += posts.size();
                break;
            case "parent_tree":
                List<Integer> parents = postsService.getParents(thread.getId(), limit, offset, desc);
                posts = postsService.getParentTreePosts(thread.getId(), parents, desc);
                offset += parents.size();
                break;
        }
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
        System.out.println("api/thread/vote");
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

        voiceService.addVote(vote, dbThread);

        return new ResponseEntity(dbThread, HttpStatus.OK);
    }
}
