package pop3;

public class POP3Line {
    
    private String commandString;
    private String[] params;
    private CommandEnum command;
    
    public POP3Line(CommandEnum command) {
	this.command = command;
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

    public String getCommandString() {
        return commandString;
    }

    public void setCommandString(String commandString) {
        this.commandString = commandString;
    }
}
