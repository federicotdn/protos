package rcp;

import java.util.HashMap;
import java.util.Map;

import exceptions.RCPException;

public class RCPParser {
	private static String spaceRegex = ".*\\s{2,}.*";
	private static String separateRegex = " ";
	private static String limitsRegex = ".*\\s$|^\\s.*";

	private static final int MAX_CMD_LEN = 6;
	private static final int MIN_CMD_LEN = 3;
	private static final int MAX_KW_LEN = 12;
	private static final int MIN_KW_LEN = 4;
	private static final int MAX_PARAM_LEN = 50;
	private static final int MAX_REQUEST_LEN = 128;

	public static final int RCP_ERROR_INVALID_CMD = 1;
	public static final int RCP_ERROR_INVALID_KW = 2;
	public static final int RCP_ERROR_UNAUTH = 3;
	public static final int RCP_ERROR_SYNTAX = 4;
	public static final int RCP_ERROR_INVALID_PARAM = 5;

	private Map<String, RCPCommandEnum> commandMap;
	private Map<String, RCPKeywordEnum> keywordMap;

	public RCPParser() {
		commandMap = new HashMap<String, RCPCommandEnum>();
		keywordMap = new HashMap<String, RCPKeywordEnum>();

		for (RCPCommandEnum cmdEnum : RCPCommandEnum.values()) {
			commandMap.put(cmdEnum.toString(), cmdEnum);
		}

		for (RCPKeywordEnum kwEnum : keywordMap.values()) {
			keywordMap.put(kwEnum.toString(), kwEnum);
		}
	}

	public RCPLine commandFromString(String com) throws RCPException {

		// TODO:
		// Corregir para matchear mejor contraseñas. PASS puede tener como
		// argumento una contraseña
		// que puede llegar a tener dos o mas espacios seguidos.

		if (com.matches(spaceRegex)) {
			throw new RCPException(RCP_ERROR_SYNTAX,
					"All separators must be single space characters.");
		}

		if (com.length() == 0 || com.matches(limitsRegex)) {
			throw new RCPException(RCP_ERROR_INVALID_CMD, "Invalid command.");
		}

		String[] lineParts = com.split(separateRegex);
		if (lineParts.length == 0) {
			throw new RCPException(RCP_ERROR_SYNTAX, "Empty line.");
		}
		
		String commandString  = lineParts[0];
		RCPCommandEnum command = commandMap.get(commandString);

		if (command == null) {
			throw new RCPException(RCP_ERROR_INVALID_CMD, "Invalid command.");
		}
		
		RCPLine commandLine = new RCPLine(command);
		
		switch (command) {
		case PASS:
			parsePass(lineParts, commandLine);
			break;
		case GET:
			parseGet(lineParts, commandLine);
			break;
		case SET:
			break;

		case QUIT:
			break;

		default:
			break;
		}

		return commandLine;
	}

	private void parsePass(String[] lineParts, RCPLine commandLine) throws RCPException {
		if (lineParts.length > 2 || lineParts.length < 2) {
			throw new RCPException(RCP_ERROR_SYNTAX, "Invalid number of parameters.");
		}
		
		String pass = lineParts[1];
		if (pass.length() > MAX_PARAM_LEN) {
			throw new RCPException(RCP_ERROR_INVALID_PARAM, "Parameter too long.");
		}
		
		//Validar ascii printable
		
		commandLine.setParameters(new String[] {lineParts[1]});
	}
	
	private void parseGet(String[] lineParts, RCPLine commandLine) throws RCPException {
		if (lineParts.length < 2) {
			throw new RCPException(RCP_ERROR_SYNTAX, "Invalid number of parameters.");
		}
		String keywordString = lineParts[1];
		if (keywordString.length() > MAX_KW_LEN) {
			throw new RCPException(RCP_ERROR_INVALID_KW, "Invalid keyword.");
		}
		
		
		RCPKeywordEnum keyword = keywordMap.get(keywordString);
		
		if (keyword == null) {
			throw new RCPException(RCP_ERROR_INVALID_KW, "Invalid keyword.");
		}
		commandLine.setKeyword(keywordString);
		
		switch (keyword) {
		case ACCESS_COUNT:
			if (lineParts.length > 2) {
				throw new RCPException(RCP_ERROR_SYNTAX, "Invalid number of parameters.");
			}
			break;
		case BYTES: 
			if (lineParts.length > 2) {
				throw new RCPException(RCP_ERROR_SYNTAX, "Invalid number of parameters.");
			}
			break;

		default:
			break;
		}
	}
	
	
}
