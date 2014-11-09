package rcp;

public enum RCPKeywordEnum {
	USER("USER"),
	USERS("USERS"),
	PASS("PASS"),
	BUFFER_SIZE("BUFFER_SIZE"),
	BYTES("BYTES"),
	ACCESS_COUNT("ACCESS_COUNT"),
	L33T_TRANSF("TRANSF"),
	L33T_TRANSFS("TRANSFS"),
	L33T("L33T"),
	MPLX("MPLX"),
	STATS("STATS"),
	DEFAULT("DEFAULT");
	
	private String keyword;
	
	private RCPKeywordEnum(String keyword) {
		this.keyword = keyword;
	}
	
	@Override
	public String toString() {
		return keyword;
	}
}
