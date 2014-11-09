package rcp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import exceptions.RCPException;

public class RCPParser {
	private static String spaceRegex = ".* {2,}.*";
	private static String separateRegex = " ";
	private static String limitsRegex = ".*\\s$|^\\s.*";
	private static final String PRINTABLE_ASCII_REGEX = "^[ -~]+$";

	private static final int MAX_CMD_LEN = 6;
	private static final int MIN_CMD_LEN = 3;
	private static final int MAX_KW_LEN = 12;
	private static final int MIN_KW_LEN = 4;
	private static final int MAX_PARAM_LEN = 50;
	public static final int MAX_REQUEST_LEN = 128;
	public static final int MAX_BUF_SIZE = 65536;

	public static final int ERROR_INVALID_CMD = 1;
	public static final int ERROR_INVALID_KW = 2;
	public static final int ERROR_UNAUTH = 3;
	public static final int ERROR_SYNTAX = 4;
	public static final int ERROR_INVALID_PARAM = 5;
	public static final int ERROR_INVALID_PASSWORD = 6;
	public static final int ERROR_INVALID_USER = 7;
	public static final int ERROR_INVALID_L33T = 8;
	public static final int ERROR_LINE_LEN = 9;
	public static final int ERROR_INTERNAL = 10;
	public static final String ENABLE_KW = "ON";
	public static final String DISABLE_KW = "OFF";

	private Map<String, RCPCommandEnum> commandMap;
	private Map<String, RCPKeywordEnum> keywordMap;

	public RCPParser() {
		commandMap = new HashMap<String, RCPCommandEnum>();
		keywordMap = new HashMap<String, RCPKeywordEnum>();

		for (RCPCommandEnum cmdEnum : RCPCommandEnum.values()) {
			commandMap.put(cmdEnum.toString(), cmdEnum);
		}

		for (RCPKeywordEnum kwEnum : RCPKeywordEnum.values()) {
			keywordMap.put(kwEnum.toString(), kwEnum);
		}
	}

	public RCPLine commandFromString(String com) throws RCPException {

		if (com.length() < MIN_CMD_LEN ) {
			throw new RCPException(ERROR_INVALID_CMD, "Invalid command.");
		}
		
		String auxCommand = com.substring(0, com.length() - 2);
		
		if (!auxCommand.matches(PRINTABLE_ASCII_REGEX)) {
			throw new RCPException(ERROR_SYNTAX,
					"All characters must be printable ASCII.");
		}
		
		if (auxCommand.matches(spaceRegex)) {
			throw new RCPException(ERROR_SYNTAX,
					"All separators must be single space characters.");
		}

		if (auxCommand.length() == 0 || com.matches(limitsRegex)) {
			throw new RCPException(ERROR_INVALID_CMD, "Invalid command.");
		}

		String[] lineParts = auxCommand.split(separateRegex);
		if (lineParts.length == 0) {
			throw new RCPException(ERROR_SYNTAX, "Empty line.");
		}

		String commandString = lineParts[0];
		
		if (!commandMap.containsKey(commandString.toUpperCase())) {
			throw new RCPException(ERROR_INVALID_CMD, "Invalid command.");
		}
		
		RCPCommandEnum command = commandMap.get(commandString.toUpperCase());
		RCPLine commandLine = new RCPLine(command);

		switch (command) {
		case PASS:
			parsePass(lineParts, commandLine);
			break;
		case GET:
			parseGet(lineParts, commandLine);
			break;
		case SET:
			parseSet(lineParts, commandLine);
			break;
		case DELETE:
			parseDelete(lineParts, commandLine);
			break;
		case QUIT:
			parseQuit(lineParts, commandLine);
			break;

		default:
			throw new RCPException(ERROR_INVALID_CMD, "Invalid command.");
		}

		return commandLine;
	}

	private void parsePass(String[] lineParts, RCPLine commandLine)
			throws RCPException {
		if (lineParts.length != 2) {
			throw new RCPException(ERROR_SYNTAX,
					"Invalid number of parameters.");
		}

		String pass = lineParts[1];
		if (pass.length() > MAX_PARAM_LEN) {
			throw new RCPException(ERROR_INVALID_PARAM,
					"Parameter too long.");
		}

		commandLine.setParameters(new String[] { lineParts[1] });
	}

	private void parseGet(String[] lineParts, RCPLine commandLine)
			throws RCPException {
		validateKeywordCommand(lineParts, commandLine);
		RCPKeywordEnum keyword = keywordMap.get(lineParts[1].toUpperCase());

		commandLine.setKeyword(keyword);

		switch (keyword) {
		case ACCESS_COUNT:
		case BYTES:
		case STATS:
		case DEFAULT:
		case BUFFER_SIZE:
		case L33T_TRANSFS:
		case USERS:
		case L33T:
		case MPLX:
			if (lineParts.length != 2) {
				throw new RCPException(ERROR_SYNTAX,
						"Invalid number of parameters.");
			}
			break;
		case USER:
			if (lineParts.length != 3) {
				throw new RCPException(ERROR_SYNTAX,
						"Invalid number of parameters.");
			}
			
			if (lineParts[2].length() > MAX_PARAM_LEN) {
				throw new RCPException(ERROR_INVALID_PARAM,
						"Parameter too long.");
			}
			commandLine.setParameters(new String[] {lineParts[2]});
			break;
		case L33T_TRANSF:
			if (lineParts.length != 3) {
				throw new RCPException(ERROR_SYNTAX,
						"Invalid number of parameters.");
			}
			
			if (lineParts[2].length() != 1) {
				throw new RCPException(ERROR_INVALID_PARAM,
						"Invalid transformation length.");
			}
			commandLine.setParameters(new String[] {lineParts[2]});
			break;
		
		default:
			throw new RCPException(ERROR_INVALID_CMD,
					"Invalid command.");
		}
	}
	
	private void parseDelete(String[] lineParts, RCPLine commandLine)
			throws RCPException {
		validateKeywordCommand(lineParts, commandLine);
		RCPKeywordEnum keyword = keywordMap.get(lineParts[1].toUpperCase());

		commandLine.setKeyword(keyword);
		
		switch (keyword) {
		case USER:
		case L33T_TRANSF:
			if (lineParts.length != 3) {
				throw new RCPException(ERROR_SYNTAX,
						"Invalid number of parameters.");
			}
			if (lineParts[2].length() > MAX_PARAM_LEN) {
				throw new RCPException(ERROR_INVALID_PARAM,
						"Parameter too long.");
			}
			commandLine.setParameters(new String[] {lineParts[2]});
			break;
		case STATS:
		case L33T_TRANSFS:
		case USERS:
		case BYTES:
		case ACCESS_COUNT:
			if (lineParts.length != 2) {
				throw new RCPException(ERROR_SYNTAX,
						"Invalid number of parameters.");
			}
			break;
		default:
			break;
		}
		
	}
	
	private void validateKeywordCommand(String[] lineParts, RCPLine commandLine) throws RCPException {
		if (lineParts.length < 2) {
			throw new RCPException(ERROR_SYNTAX,
					"Invalid number of parameters.");
		}
		String keywordString = lineParts[1];
		if (keywordString.length() > MAX_KW_LEN) {
			throw new RCPException(ERROR_INVALID_KW, "Invalid keyword.");
		}
		
		if (!keywordMap.containsKey(keywordString.toUpperCase())) {
			throw new RCPException(ERROR_INVALID_KW, "Invalid keyword.");
		}
	}
	
	private void parseSet(String[] lineParts, RCPLine commandLine) throws RCPException {
		validateKeywordCommand(lineParts, commandLine);
		RCPKeywordEnum keyword = keywordMap.get(lineParts[1].toUpperCase());

		commandLine.setKeyword(keyword);
		
		switch (keyword) {
		case BUFFER_SIZE:
		case PASS:
		case DEFAULT:
			if (lineParts.length != 3) {
				throw new RCPException(ERROR_SYNTAX,
						"Invalid number of parameters.");
			}
			if (lineParts[2].length() > MAX_PARAM_LEN) {
				throw new RCPException(ERROR_INVALID_PARAM,
						"Parameter too long.");
			}
			commandLine.setParameters(new String[] {lineParts[2]});
			break;
		case MPLX:
		case L33T:
			if (lineParts.length != 3) {
				throw new RCPException(ERROR_SYNTAX,
						"Invalid number of parameters.");
			}
			if (lineParts[2].length() > MAX_PARAM_LEN) {
				throw new RCPException(ERROR_INVALID_PARAM,
						"Parameter too long.");
			}
			if (!lineParts[2].toUpperCase().equals(ENABLE_KW) && !lineParts[2].toUpperCase().equals(DISABLE_KW)) {
				throw new RCPException(ERROR_INVALID_PARAM,
						"Invalid param. Only valid parameters are 'ON' or 'OFF'");
			}
			
			commandLine.setParameters(new String[] {lineParts[2]});
			break;
		case USER:
			if (lineParts.length != 4) {
				throw new RCPException(ERROR_SYNTAX,
						"Invalid number of parameters.");
			}
			if (lineParts[2].length() > MAX_PARAM_LEN || lineParts[3].length() > MAX_PARAM_LEN) {
				throw new RCPException(ERROR_INVALID_PARAM,
						"Parameter too long.");
			}
			commandLine.setParameters(Arrays.copyOfRange(lineParts, 2, lineParts.length));
			break;
		case L33T_TRANSF:
			if (lineParts.length != 4) {
				throw new RCPException(ERROR_SYNTAX,
						"Invalid number of parameters.");
			}
			if (lineParts[2].length() != 1 || lineParts[3].length() != 1) {
				throw new RCPException(ERROR_INVALID_PARAM,
						"Invalid parameter length.");
			}
			commandLine.setParameters(Arrays.copyOfRange(lineParts, 2, lineParts.length));
			break;
		default:
			throw new RCPException(ERROR_INVALID_CMD,
					"Invalid command.");
		}
	}
	
	private void parseQuit(String[] lineParts, RCPLine commandLine) throws RCPException {
		if (lineParts.length > 1) {
			throw new RCPException(ERROR_SYNTAX,
					"Invalid number of parameters.");
		}
	}

}
