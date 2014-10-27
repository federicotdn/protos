package pop3;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import config.ConfigLoader;
import config.POP3Values;
import exceptions.InvalidCommandException;

public class POP3CommandParser {
    
    private static String spaceRegex = ".*\\s{2,}.*";
    private static String separateRegex = "\\s{1}";
    
    private Map<String, POP3Command> pop3Commands;
    private POP3Values pop3Vals;
    
    public POP3CommandParser(String protocolFile) throws IOException, JAXBException {
	
	pop3Vals = ConfigLoader.loadPOP3Values(protocolFile);
	pop3Commands = new HashMap<String, POP3Command>();
	
	Map<String, String> pop3CommandDefs = pop3Vals.getCommandMap();
	
	for (POP3Command com : POP3Command.values()) {
	    
	    String commandName = pop3CommandDefs.get(com.getKey());
	    pop3Commands.put(commandName, com);
	    
	}
    }
    
    public POP3Command commandFromString(String com) throws InvalidCommandException {
	
	if (com.matches(spaceRegex)) {
	    throw new InvalidCommandException(com, "all separators must be single space characters.");
	}
	
	String[] comParts = com.split(separateRegex);
	
	for (String part : comParts) {
	    if (part.length() > pop3Vals.getMaxParamLen()) {
		
	    }
	}
	
	int len = com.length();
	if (len < pop3Vals.getMinCommandLen() || len > pop3Vals.getMaxCommandLen()) {
	    throw new InvalidCommandException(com, "invalid command length.");
	}
	
	String comUpper = com.toUpperCase();
	POP3Command pop3Command = pop3Commands.get(comUpper);
	
	if (pop3Command == null) {
	    throw new InvalidCommandException(com, "command does not exist.");
	}
	
	return pop3Command;
    }
    
    public String getCommandString(POP3Command com) {
	return pop3Vals.getCommandMap().get(com.getKey());
    }
    
}
