package exceptions;

public class InvalidCommandException extends Exception {
    private String command;
    
    public InvalidCommandException(String command) {
	this.command = command;
    }
    
    @Override
    public String getMessage() {
	return "Invalid POP3 command: " + command;
    }
    
}
