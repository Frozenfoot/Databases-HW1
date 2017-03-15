//package DB.Controllers;
//
//import DB.Models.Forum;
//import DB.Models.ForumThread;
//import DB.Models.Post;
//import DB.Models.Vote;
//import org.eclipse.jetty.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * Created by frozenfoot on 15.03.17.
// */
//@RestController
//@RequestMapping("thread/{slug_or_id}")
//public class ThreadController {
//
//    JdbcTemplate jdbcTemplate = new JdbcTemplate();
////  ThreadService threadservice;
//
//    @RequestMapping(
//            value = "/create",
//            method = RequestMethod.POST,
//            produces = MediaType.APPLICATION_JSON_VALUE,
//            consumes = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<List<Post>> addPosts(
//            @PathVariable("slug_or_id") String slug,
//            @RequestBody List<Post> posts
//    ) {
//
//    }
//
//    @RequestMapping(
//            value = "/details",
//            method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<ForumThread> getThreadDetails(@PathVariable("slug_or_id") String slug,){
//
//    }
//
//    @RequestMapping(
//            value = "/details",
//            method = RequestMethod.POST,
//            produces = MediaType.APPLICATION_JSON_VALUE,
//            consumes = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<ForumThread> changeThreadDetails(
//            @PathVariable("slug_or_id") String slug,
//            @RequestBody ForumThread thread
//    ) {
//
//    }
//
//    @RequestMapping(
//            value = "/posts",
//            method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<PostsInThread> getPostsInThread(
//            @PathVariable("slug_or_id") String slug,
//            @RequestParam(value = "limit", required = false, defaultValue = 100) int limit,
//            @RequestParam(value = "marker", required = false) String marker,
//            @RequestParam(value = "sort", required = false, defaultValue = "flat") String sort,
//            @RequestParam(value = "desc", required = false) Boolean desc
//    ) {
//
//    }
//
//    @RequestMapping(
//            value = "/vote",
//            method = RequestMethod.POST,
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<ForumThread> voteForThread(
//            @PathVariable("slug_or_id") String slug,
//            @RequestBody Vote vote
//    ) {
//
//    }
//}
