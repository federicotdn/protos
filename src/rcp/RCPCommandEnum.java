package rcp;

public enum RCPCommandEnum {
	
	PASS("PASS"),
	SET("SET"),
	GET("GET"),
	DELETE("DELETE"),
	QUIT("QUIT");
	
	private String command;
	
	private RCPCommandEnum(String command) {
		this.command = command;
	}
	
	@Override
	public String toString() {
		return command;
	}

}
