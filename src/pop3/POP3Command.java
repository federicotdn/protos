package pop3;

public enum POP3Command {
    
    QUIT("quit"),
    USER("user"),
    PASS("password"),
    STAT("stats"),
    LIST("list"),
    RETR("retrieve"),
    DELE("delete"),
    CAPA("capabilities"),
    OK("ok"),
    ERROR("error");
    
    private final String key;
    private String[] params;
    
    POP3Command(String key) {
	this.key = key;
	params = null;
    }
    
    public String[] getParams() {
	return params;
    }
    
    public void setParams(String[] params) {
	this.params = params;
    }
    
    public String getKey() {
	return key;
    }
    
    @Override
    public String toString() {
	return key;
    }
    
}
