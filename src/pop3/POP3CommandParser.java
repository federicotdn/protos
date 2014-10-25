package pop3;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import exceptions.InvalidCommandException;

public class POP3CommandParser {
    
    private int pop3MaxLen, pop3MinLen;
    
    private static String MAXLEN_KEY = "max_len";
    private static String MINLEN_KEY = "min_len";
    
    private Map<String, POP3Command> pop3Commands;
    Properties properties;
    
    public POP3CommandParser(String protocolFile) throws IOException {
	
	pop3Commands = new HashMap<String, POP3Command>();
	
	properties = new Properties();
	InputStream fileInput = getClass().getClassLoader().getResourceAsStream(protocolFile);
	
	properties.load(fileInput);
	
	pop3MaxLen = Integer.valueOf(properties.getProperty(MAXLEN_KEY));
	pop3MinLen = Integer.valueOf(properties.getProperty(MINLEN_KEY));
	
	for (POP3Command com : POP3Command.values()) {
	    
	    String commandName = properties.getProperty(com.getKey());
	    pop3Commands.put(commandName, com);
	    
	}
    }
    
    public POP3Command commandFromString(String com) throws InvalidCommandException {
	
	int len = com.length();
	if (len < pop3MinLen || len > pop3MaxLen) {
	    throw new InvalidCommandException(com);
	}
	
	String comUpper = com.toUpperCase();
	POP3Command pop3Command = pop3Commands.get(comUpper);
	
	if (pop3Command == null) {
	    throw new InvalidCommandException(com);
	}
	
	return pop3Command;
    }
    
    public String getCommandString(POP3Command com) {
	return properties.getProperty(com.getKey());
    }
    
}
