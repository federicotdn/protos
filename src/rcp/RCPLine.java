package rcp;

public class RCPLine {
	private String commandString;
	private RCPCommandEnum command;
	private String keyword;
	private String[] parameters;

	public RCPLine(RCPCommandEnum command) {
		this.command = command;
	}

	public String getCommandString() {
		return commandString;
	}

	public void setCommandString(String commandString) {
		this.commandString = commandString;
	}

	public RCPCommandEnum getCommand() {
		return command;
	}

	public void setCommand(RCPCommandEnum command) {
		this.command = command;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String[] getParameters() {
		return parameters;
	}

	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}
}
