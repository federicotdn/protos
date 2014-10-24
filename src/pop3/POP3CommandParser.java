package pop3;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class POP3CommandParser {
    
    private Map<String, POP3Command> pop3Commands;
    
    public POP3CommandParser(String protocolFile) throws IOException {
	
	pop3Commands = new HashMap<String, POP3Command>();
	
	Properties properties = new Properties();
	InputStream fileInput = getClass().getClassLoader().getResourceAsStream(protocolFile);
	
	properties.load(fileInput);
	
	
    }
    
}
