package pop3;

public class POP3Line {
    
    private String commandString;
    private String[] params;
    private CommandEnum command;
    private boolean valid;
    private String error;
    
    public POP3Line() {
	this.valid = false;
	this.command = null;
    }
    
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isValid() {
        return valid;
    }
    
    public String[] getParams() {
	return params;
    }
    
    public void setParams(String[] params) {
	this.params = params;
    }
    
    public CommandEnum getCommand() {
        return command;
    }
    
    public void setCommand(CommandEnum command) {
	this.command = command;
	valid = (command != null);
    }

    public String getCommandString() {
        return commandString;
    }

    public void setCommandString(String commandString) {
        this.commandString = commandString;
    }
}
