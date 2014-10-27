package exceptions;

public class InvalidCommandException extends Exception {
    private String msg;
    
    public InvalidCommandException(String msg) {
	this.msg = msg;
    }
    
    @Override
    public String getMessage() {
	return msg;
    }
    
}
