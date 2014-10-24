package pop3;

public enum POP3Command {
    
    QUIT("quit"),
    USER("user"),
    PASS("password"),
    STAT("stats"),
    LIST("list"),
    RETR("retrieve"),
    DELE("delete"),
    CAPA("capabilities");
    
    private final String key;
    
    POP3Command(String key) {
	this.key = key;
    }
    
    public String getKey() {
	return key;
    }
    
}
