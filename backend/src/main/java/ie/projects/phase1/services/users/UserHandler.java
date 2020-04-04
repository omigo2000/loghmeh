package ie.projects.phase1.services.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.projects.phase1.core.Loghmeh;
import ie.projects.phase1.core.User;
import ie.projects.phase1.exceptions.NegativeCreditAmount;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class UserHandler {
    Loghmeh loghmeh = Loghmeh.getInstance();
    ObjectMapper mapper = new ObjectMapper();


    @RequestMapping(value = "/v1/profile", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public String getUserProfile() throws IOException {
        User user = loghmeh.getUsers().get(0);
        return mapper.writeValueAsString(user);
    }

    @RequestMapping(value = "/v1/credit", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public String getRestaurant(@RequestBody AddCreditRequest request) throws IOException, NegativeCreditAmount {
        User user = loghmeh.getUsers().get(0);
        user.addCredit(request.getAmount());
        return mapper.writeValueAsString(user);
    }
}