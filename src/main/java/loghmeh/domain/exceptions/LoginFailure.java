package loghmeh.domain.exceptions;

public class LoginFailure extends Exception{
    public LoginFailure(String msg){
        super(msg);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
