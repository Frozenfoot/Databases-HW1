//package DB.Controllers;
//
//import DB.Application;
//import DB.Models.User;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.web.bind.annotation.*;
//
///**
// * Created by frozenfoot on 15.03.17.
// */
//@RestController
//@RequestMapping("user/{nickname}")
//public class UserController {
//
//    JdbcTemplate jdbcTemplate = new JdbcTemplate();
////  UserService userService = new UserService();
//
//    @RequestMapping(
//            value = "/create",
//            method = RequestMethod.POST,
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<User> createUser(
//            @RequestBody User user,
//            @PathVariable("nickname") String nickname
//    ) {
//
//    }
//
//    @RequestMapping(
//            value = "/profile",
//            method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<User> getUser(@PathVariable("nickname") String nickname){
//
//    }
//
//    @RequestMapping(
//            value = "/profile",
//            method = RequestMethod.POST,
//            produces = MediaType.APPLICATION_JSON_VALUE,
//            consumes = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<User> changeUser(
//            @PathVariable("nickname") String nickname,
//            @RequestBody User user
//    ) {
//
//    }
//}
