package pop3;

public enum CommandEnum {
    
    QUIT("QUIT"),
    USER("USER"),
    PASS("PASS"),
    STAT("STAT"),
    LIST("LIST"),
    RETR("RETR"),
    DELE("DELE"),
    CAPA("CAPA"),
    NOOP("NOOP"),
    RSET("RSET"),
    OK("+OK"),
    ERR("-ERR");
    
    private String commandString;
    
    CommandEnum(String commandString) {
	this.commandString = commandString;
    }
    
    @Override
    public String toString() {
	return commandString;
    }
}
