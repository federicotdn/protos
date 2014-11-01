package pop3;

public class POP3Command {
    
    private CommandEnum command;
    
    private String originalCommand;

    private String[] params;
    
    public POP3Command(CommandEnum command) {
	this.command = command;
    }
    
    public CommandEnum getCommand() {
	return command;
    }
    
    public String[] getParams() {
	return params;
    }
    
    public void setParams(String[] params) {
	this.params = params;
    }
    
    public String getOriginalCommand() {
        return originalCommand;
    }

    public void setOriginalCommand(String originalCommand) {
        this.originalCommand = originalCommand;
    }
}
