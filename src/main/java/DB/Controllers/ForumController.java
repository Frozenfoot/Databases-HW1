package DB.Controllers;

import DB.Models.Forum;
import DB.Models.User;
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

    private final JdbcTemplate jdbcTemplate;
    private final ForumService forumService;

    public ForumController(JdbcTemplate jdbcTemplate, ForumService forumService) {
        this.jdbcTemplate = jdbcTemplate;
        this.forumService = forumService;
    }

    @RequestMapping(
            value = "/create",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Forum> createForum(@RequestBody Forum forum){

    }

    @RequestMapping(
            value = "/{slug}/create",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Thread> createThread(
            @RequestBody Thread thread,
            @PathVariable("slug") String slug) {

    }

    @RequestMapping(
            value = "/{slug}/details",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Forum> getForumDetails(@PathVariable("slug") String slug){

    }

    @RequestMapping(
            value = "/{slug}/threads",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Thread>> getForumThreads(
            @PathVariable("slug") String slug,
            @RequestParam(value = "limit", required = false, defaultValue = 100) int limit,
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "desc", required = false) Boolean desc
    ) {

    }

    @RequestMapping(
            value = "/{slug}/users",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<User>> getForumUsers(
            @PathVariable("slug") String slug,
            @RequestParam(value = "limit", required = false, defaultValue = 100) int limit,
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "desc", required = false) Boolean desc
    ) {

    }
}
