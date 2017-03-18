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
            System.out.println(e.toString());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (DataIntegrityViolationException e) {
            System.out.println(e.toString());
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
        try {
            thread.setSlug(slug);
            threadService.addThread(thread);
            return new ResponseEntity(threadService.getThread(thread.getSlug()), HttpStatus.CREATED);
        } catch (DuplicateKeyException e) {
            System.out.println(e.toString());
            return new ResponseEntity<>(threadService.getThread(thread.getSlug()), HttpStatus.CONFLICT);
        } catch (DataIntegrityViolationException e) {
            System.out.println(e.toString());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
            return new ResponseEntity<>(threadService.getThreads(slug, limit, since, desc), HttpStatus.OK);
        }
        catch (EmptyResultDataAccessException e){
            System.out.println(e.toString());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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
        try{
            return new ResponseEntity(userService.getForumUsers(slug, limit, since, desc), HttpStatus.OK);
        }
        catch (EmptyResultDataAccessException e){
            System.out.println(e.toString());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
