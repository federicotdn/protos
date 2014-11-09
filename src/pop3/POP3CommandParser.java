package pop3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class POP3CommandParser {
    
    private static final String printableRegex = "^[ -~]+$";
    private static final String spaceRegex = ".* {2,}.*";
    private static final String separateRegex = "\\s{1}";
    private static final String limitsRegex = ".*\\s$|^\\s.*";
    
    public static final int MAX_CMD_LEN = 4;
    public static final int MIN_CMD_LEN = 3;
    public static final int MAX_PARAM_LEN = 40;
    public static final int MAX_REQ_LEN = 255;
    public static final int MAX_RESP_LEN = 512;
    
    private Map<String, CommandEnum> commandMap;
    
    public POP3CommandParser() {
	
	commandMap = new HashMap<String, CommandEnum>();
	
	for (CommandEnum com : CommandEnum.values()) {
	    
	    commandMap.put(com.toString(), com);
	    
	}
	
    }
    
    
    public POP3Line commandFromString(StringBuffer commandBuffer) {
	
	POP3Line userCommand = new POP3Line();
	String com;
	int commandLen = commandBuffer.length();
	
	if (commandLen < MIN_CMD_LEN) {
	    userCommand.setError("Invalid command length.");
	    return userCommand;
	}
	
	com = commandBuffer.substring(0, commandLen - 2);
	
	if (!com.matches(printableRegex)) {
	    userCommand.setError("Commands should use printable ASCII characters.");
	    return userCommand;
	}
	
	if (com.matches(spaceRegex)) {
	    userCommand.setError("All separators must be single space characters.");
	    return userCommand;
	}
	
	if (com.length() == 0 || com.matches(limitsRegex)) {
	    userCommand.setError("Malformed command.");
	    return userCommand;
	}
	
	String[] comParts = com.split(separateRegex);
	
	for (String part : comParts) {
	    if (part.length() > MAX_PARAM_LEN) {
		userCommand.setError("Invalid parameter length.");
		return userCommand;
	    }
	}
	
	String firstCommand = comParts[0];
	int len = firstCommand.length();
	
	if (len < MIN_CMD_LEN || len > MAX_CMD_LEN) {
	    userCommand.setError("Invalid command length.");
	    return userCommand;
	}
	
	String comUpper = firstCommand.toUpperCase();
	CommandEnum pop3Com = commandMap.get(comUpper);
	
	if (comParts.length > 1) {
	    userCommand.setParams(Arrays.copyOfRange(comParts, 1, comParts.length));
	}
	
	userCommand.setCommand(pop3Com);
	
	if (pop3Com == null) {
	    userCommand.setError("Unknown command.");
	}
	
	userCommand.setCommandString(com);
	return userCommand;
    }
}
