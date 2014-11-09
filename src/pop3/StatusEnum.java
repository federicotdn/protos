package pop3;

public enum StatusEnum {
    READ(1), WRITE(2), CLOSING(3), GREETING(4);
    
    int val;
    
    StatusEnum(int position) {
	this.val = (1 << position);
    }
    
    public int getVal() {
	return val;
    }
}
