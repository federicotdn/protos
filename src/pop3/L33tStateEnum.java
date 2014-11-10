package pop3;

public enum L33tStateEnum {
	START(),
	Q1(),
	Q2(),
	ENCODING(),
	Q3(),
	Q4(),
	SKIP_TO_END(),
	ASCII_TRANSOFRM(),
	ENC_END_R(),
	ENC_END_N(),
	ASCII_NL(),
	ASCII_END_N(),
	WS(),
	SKIP_SUBJECT(),
	FINAL_EQUALS(),
	FIRST_EQUALS(),
	SUBJECT_ENDED(),
	SE_END_N(),
	SE_END_R(),
	SE_END_N2(),
	SE_DOT();
}
