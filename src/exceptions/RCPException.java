package exceptions;

public class RCPException extends Exception{
	private int errorCode;
	
	public RCPException(int errorCode, String msg) {
		super(msg);
		this.errorCode = errorCode;
	}

	public RCPException(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}
