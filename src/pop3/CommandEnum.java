package pop3;

public enum CommandEnum {
    
    QUIT("quit"),
    USER("user"),
    PASS("password"),
    STAT("stats"),
    LIST("list"),
    RETR("retrieve"),
    DELE("delete"),
    CAPA("capabilities"),
    OK("ok"),
    ERROR("error"),
    NOOP("no_operation"),
    RSET("reset");
    
    private final String key;
    
    CommandEnum(String key) {
	this.key = key;
    }
    
    public String getKey() {
	return key;
    }
    
    @Override
    public String toString() {
	return key;
    }
}
