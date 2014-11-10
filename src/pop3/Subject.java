package pop3;

public class Subject {
	private static final int SPECIAL_CHARS = 8;
	private StringBuffer charset;
	private String encoding;
	private StringBuffer text;
	private StringBuffer original;
	
	public Subject() {
		reset();
	}

	public StringBuffer getCharset() {
		return charset;
	}


	public String getEncoding() {
		return encoding;
	}
	
	public void appendText(char c) {
		this.text.append(c);
		this.original.append(c);
	}
	
	public void appendCharset(char c) {
		this.charset.append(c);
		this.original.append(c);
	}

	public void setEncoding(String encoding) {
		this.original.append(encoding);
		this.encoding = encoding;
	}
	
	public void appendOriginal(char c) {
		this.original.append(c);
	}

	public StringBuffer getText() {
		return text;
	}

	public void setText(StringBuffer text) {
		this.text = text;
	}
	
	public void reset() {
		charset = new StringBuffer();
		encoding = new String();
		original = new StringBuffer();
		text = new StringBuffer();
	}
	

	
	public static int getSpecialChars() {
		return SPECIAL_CHARS;
	}

	public int lenght() {
		return original.length();
	}
	
	public String getOriginal() {
		return original.toString();
	}
	
	public String toEncodedString(String text) {
		return "=?" + charset + "?" + encoding + "?" + text + "?=\r\n";
	}
	
	
}
