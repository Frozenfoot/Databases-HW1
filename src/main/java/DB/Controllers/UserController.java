package DB.Controllers;

import DB.Models.User;
import DB.Services.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * Created by frozenfoot on 15.03.17.
 */
@RestController
@RequestMapping("user/{nickname}")
public class UserController {

      JdbcTemplate jdbcTemplate;
      UserService userService;

      public UserController(JdbcTemplate jdbcTemplate){
          this.jdbcTemplate = jdbcTemplate;
          userService = new UserService(jdbcTemplate);
      }

    @RequestMapping(
            value = "/create",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity createUser(
            @RequestBody User user,
            @PathVariable("nickname") String nickname
    ) {
          user.setNickname(nickname);
          try {
              userService.addUser(user);
          }
          catch (DuplicateKeyException e){
              System.out.println(e.toString());
              return new ResponseEntity<>(userService.getUser(user), HttpStatus.CONFLICT);
          }
          return new ResponseEntity<>(userService.getUser(nickname), HttpStatus.CREATED);
    }

    @RequestMapping(
            value = "/profile",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getUser(@PathVariable("nickname") String nickname){
          try{
              return new ResponseEntity<> (userService.getUser(nickname), HttpStatus.ACCEPTED);
          }
          catch (EmptyResultDataAccessException e){
              System.out.println(e.toString());
              return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
          }
    }

    @RequestMapping(
            value = "/profile",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity changeUser(
            @PathVariable("nickname") String nickname,
            @RequestBody User user
    ) {
        try{
            user.setNickname(nickname);
            userService.updateUser(user);
            return new ResponseEntity<>(userService.getUser(nickname), HttpStatus.OK);
        }
        catch (EmptyResultDataAccessException e){
            System.out.println(e.toString());
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        catch (DuplicateKeyException e){
            System.out.println(e.toString());
            return new ResponseEntity<>("", HttpStatus.CONFLICT);
        }

    }
}
