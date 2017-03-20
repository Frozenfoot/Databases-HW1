package DB.Controllers;

import DB.Models.Forum;
import DB.Models.ForumThread;
import DB.Models.User;
import DB.Services.ForumService;
import DB.Services.ThreadService;
import DB.Services.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by frozenfoot on 15.03.17.
 */
@RestController
@RequestMapping("forum")
public class ForumController {

    private ForumService forumService;
    private JdbcTemplate jdbcTemplate;
    private ThreadService threadService;
    private UserService userService;

    public ForumController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.forumService = new ForumService(jdbcTemplate);
        this.threadService = new ThreadService(jdbcTemplate);
        this.userService = new UserService(jdbcTemplate);
    }

    @RequestMapping(
            value = "/create",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity createForum(@RequestBody Forum forum) {
        try {
            forumService.addForum(forum);
            return new ResponseEntity<>(forumService.getForum(forum.getSlug()), HttpStatus.CREATED);
        } catch (DuplicateKeyException e) {
            return new ResponseEntity<>(forumService.getForum(forum.getSlug()), HttpStatus.CONFLICT);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @RequestMapping(
            value = "/{slug}/create", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity createThread(
            @RequestBody ForumThread thread,
            @PathVariable("slug") String slug) {
        Forum forum;
        try {
            forum = forumService.getForum(slug);
        }
        catch (EmptyResultDataAccessException e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        try {
            thread.setForum(forum.getSlug());
            int id = threadService.addThread(thread);
            ForumThread result = threadService.getThread(id);
            if (thread.getForum().equals(thread.getSlug())){
                result.setSlug(null);
            }

            return new ResponseEntity(result, HttpStatus.CREATED);
        } catch (DuplicateKeyException e) {
            return new ResponseEntity (threadService.getThread(thread.getSlug()), HttpStatus.CONFLICT);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity (HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(
            value = "/{slug}/details",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getForumDetails(@PathVariable("slug") String slug) {
        try {
            return new ResponseEntity(forumService.getForum(slug), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(
            value = "/{slug}/threads",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getForumThreads(
            @PathVariable("slug") String slug,
            @RequestParam(value = "limit", required = false, defaultValue = "100") int limit,
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "desc", required = false) Boolean desc
    ) {
        try{
            forumService.getForum(slug);
        }
        catch (EmptyResultDataAccessException e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
            List<ForumThread> result = threadService.getThreads(slug, limit, since, desc);
            return new ResponseEntity (result, HttpStatus.OK);
    }


    @RequestMapping(
            value = "/{slug}/users",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getForumUsers(
            @PathVariable("slug") String slug,
            @RequestParam(value = "limit", required = false, defaultValue = "100") int limit,
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "desc", required = false) Boolean desc
    ) {
        System.out.println("Slug is: " + slug);
        try{
            Forum forum = forumService.getForum(slug);
            return new ResponseEntity(userService.getForumUsers(slug, limit, since, desc), HttpStatus.OK);
        }
        catch (EmptyResultDataAccessException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
