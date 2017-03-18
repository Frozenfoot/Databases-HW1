package DB.Controllers;

import DB.Services.ServiceService;
import org.eclipse.jetty.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by frozenfoot on 15.03.17.
 */
@RestController
@RequestMapping("service")
public class ServiceController {

    private JdbcTemplate jdbcTemplate;
    private ServiceService service;

    public ServiceController(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
        this.service = new ServiceService(jdbcTemplate);
    }

    @RequestMapping(
            value = "/status",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getDatabaseStatus(){
        return new ResponseEntity(service.getServiceStatus(), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/clear",
            method = RequestMethod.POST
    )
    public ResponseEntity clearDatabase(){
        service.clearService();
        return new ResponseEntity(HttpStatus.OK);
    }
}
