package loghmeh.domain.exceptions;

public class DuplicateEmail extends Exception{
    public DuplicateEmail(String msg){
        super(msg);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
