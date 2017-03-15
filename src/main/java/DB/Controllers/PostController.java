//package DB.Controllers;
//
//import DB.Models.Post;
//import org.eclipse.jetty.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.web.bind.annotation.*;
//
///**
// * Created by frozenfoot on 15.03.17.
// */
//@RestController
//@RequestMapping("post/{id}")
//public class PostController {
//
//    private JdbcTemplate jdbcTemplate;
////    private PostService postService;
//
//    @RequestMapping(
//            value = "/details",
//            method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<PostDetails> getPostDetails(
//            @PathVariable("id") int id,
//            @RequestParam("related") String[] related){
//
//    }
//
//    @RequestMapping(
//            value = "/details",
//            method = RequestMethod.POST,
//            produces = MediaType.APPLICATION_JSON_VALUE,
//            consumes = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<Post> changePostDetails(
//            @PathVariable("id") int id,
//            @RequestBody Post post
//    ) {
//
//    }
//
//}
