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
    
    private static final String spaceRegex = ".*\\s{2,}.*";
    private static final String separateRegex = "\\s{1}";
    private static final String limitsRegex = ".*\\s$|^\\s.*";
    
    private static final int MAX_CMD_LEN = 4;
    private static final int MIN_CMD_LEN = 3;
    private static final int MAX_PARAM_LEN = 40;
    private static final int MAX_REQ_LEN = 255;
    
    public Integer getMaxRequestLen() {
	return MAX_REQ_LEN;
    }
    
    public POP3Line commandFromString(String com) throws InvalidCommandException {
	
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
	    if (part.length() > MAX_PARAM_LEN) {
		throw new InvalidCommandException("Invalid parameter length.");
	    }
	}
	
	String firstCommand = comParts[0];
	int len = firstCommand.length();
	
	if (len < MIN_CMD_LEN || len > MAX_CMD_LEN) {
	    throw new InvalidCommandException("Invalid command length.");
	}
	
	String comUpper = firstCommand.toUpperCase();
	
	POP3Line userCommand = new POP3Line(comUpper);
	
	if (comParts.length > 1) {
	    userCommand.setParams(Arrays.copyOfRange(comParts, 1, comParts.length));
	}
	
	userCommand.setCommandString(com);
	return userCommand;
    }
}
