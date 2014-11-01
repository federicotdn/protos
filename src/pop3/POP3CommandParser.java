package pop3;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import config.ConfigLoader;
import config.POP3Values;
import exceptions.InvalidCommandException;

public class POP3CommandParser {
    
    private static String spaceRegex = ".*\\s{2,}.*";
    private static String separateRegex = "\\s{1}";
    private static String limitsRegex = ".*\\s$|^\\s.*";
    
    private Map<String, CommandEnum> pop3Commands;
    private POP3Values pop3Vals;
    
    public POP3CommandParser(String protocolFile) throws IOException, JAXBException {
	
	pop3Vals = ConfigLoader.loadPOP3Values(protocolFile);
	pop3Commands = new HashMap<String, CommandEnum>();
	
	Map<String, String> pop3CommandDefs = pop3Vals.getCommandMap();
	
	for (CommandEnum com : CommandEnum.values()) {
	    
	    String commandName = pop3CommandDefs.get(com.getKey());
	    pop3Commands.put(commandName, com);
	    
	}
    }
    
    public Integer getMaxRequestLen() {
	return pop3Vals.getMaxRequestLen();
    }
    
    public POP3Command commandFromString(String com) throws InvalidCommandException {
	
	//TODO:
	//Corregir para matchear mejor contraseñas.  PASS puede tener como argumento una contraseña
	//que puede llegar a tener dos o mas espacios seguidos.
	
	if (com.matches(spaceRegex)) {
	    throw new InvalidCommandException("All separators must be single space characters.");
	}
	
	if (com.length() == 0 || com.matches(limitsRegex)) {
	    throw new InvalidCommandException("Invalid command.");
	}
	
	String[] comParts = com.split(separateRegex);
	
	for (String part : comParts) {
	    if (part.length() > pop3Vals.getMaxParamLen()) {
		throw new InvalidCommandException("Invalid parameter length.");
	    }
	}
	
	String firstCommand = comParts[0];
	int len = firstCommand.length();
	
	if (len < pop3Vals.getMinCommandLen() || len > pop3Vals.getMaxCommandLen()) {
	    throw new InvalidCommandException("Invalid command length.");
	}
	
	String comUpper = firstCommand.toUpperCase();
	CommandEnum command = pop3Commands.get(comUpper);
	
	if (command == null) {
	    throw new InvalidCommandException("Command does not exist.");
	}
	
	POP3Command userCommand = new POP3Command(command);
	
	if (comParts.length > 1) {
	    userCommand.setParams(Arrays.copyOfRange(comParts, 1, comParts.length));
	}
	
	userCommand.setOriginalCommand(com);
	return userCommand;
    }
    
    public String getCommandString(CommandEnum com) {
	return pop3Vals.getCommandMap().get(com.getKey());
    }
    
}
