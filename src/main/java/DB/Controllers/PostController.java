package DB.Controllers;

import DB.Models.Post;
import DB.Services.ForumService;
import DB.Services.PostsService;
import DB.Services.ThreadService;
import DB.Services.UserService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * Created by frozenfoot on 15.03.17.
 */
@RestController
@RequestMapping("post/{id}")
public class PostController {

    private JdbcTemplate jdbcTemplate;
    private PostsService postService;
    private UserService userService;
    private ForumService forumService;
    private ThreadService threadService;

    public PostController(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
        postService = new PostsService(jdbcTemplate);
        userService = new UserService(jdbcTemplate);
        forumService = new ForumService(jdbcTemplate);
        threadService = new ThreadService(jdbcTemplate);
    }

    @RequestMapping(
            value = "/details",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getPostDetails(
            @PathVariable("id") int id,
            @RequestParam("related") ArrayList<String> related) throws JSONException {

        Post post;
        try{
            post = postService.getPost(id);
        }
        catch (EmptyResultDataAccessException e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        JSONObject result = new JSONObject();
        if(related != null){

            if(related.contains("user")){
                result.put("author", userService.getUser(post.getAuthor()));
            }

            if(related.contains("forum")){
                result.put("forum", forumService.getForum(post.getForum()));
            }

            if (related.contains("thread")){
                result.put("thread", threadService.getThread(post.getThread()));
            }
        }
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/details",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity changePostDetails(
            @PathVariable("id") int id,
            @RequestBody Post post
    ) {
        try{
            postService.changePost(post, id);
            return new ResponseEntity(postService.getPost(id), HttpStatus.OK);
        }
        catch (EmptyResultDataAccessException e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}
