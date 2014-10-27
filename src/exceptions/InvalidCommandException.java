package exceptions;

public class InvalidCommandException extends Exception {
    private String command;
    private String reason;
    
    public InvalidCommandException(String command, String reason) {
	this.command = command;
	this.reason = reason;
    }
    
    @Override
    public String getMessage() {
	return "Invalid command: " + command + ": " + reason;
    }
    
}
