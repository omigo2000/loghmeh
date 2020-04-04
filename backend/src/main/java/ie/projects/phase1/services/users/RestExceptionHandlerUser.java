package ie.projects.phase1.services.users;

import ie.projects.phase1.exceptions.NegativeCreditAmount;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandlerUser extends ResponseEntityExceptionHandler {
    @ExceptionHandler(NegativeCreditAmount.class)
    public ResponseEntity<Object> negativeAmount(Exception ex){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
