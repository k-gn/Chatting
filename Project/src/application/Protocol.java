package application;

public final class Protocol {

	public static final int LOGIN = 100;
	public static final int LOGOUT = 101;
	public static final int CREATE_ROOM = 102;
	public static final int ENTER_ROOM = 103;
	public static final int QUIT_ROOM = 104;
	public static final int SEND_WORD = 105;
	public static final int SEND_WORD_TO = 106;
	public static final int COERCE_OUT = 107;
	public static final int SEND_FILE = 108;
	
	public static final int OK_LOGIN = 200;
	public static final int NO_LOGIN = 201;
	public static final int OK_LOGOUT = 202;
	public static final int OK_CREATE_ROOM = 203;
	public static final int NO_CREATE_ROOM = 204;
	public static final int OK_ENTER_ROOM = 205;
	public static final int NO_ENTER_ROOM = 206;
	public static final int OK_QUIT_ROOM = 207;
	
	public static final int OK_SEND_WORD = 210;
	public static final int OK_SEND_WORD_TO = 211;
	public static final int NO_SEND_WORD_TO = 212;
	public static final int OK_COERCE_OUT = 213;
	public static final int OK_SEND_FILE = 214;
	public static final int NO_SEND_FILE = 215;
	
	public static final int WAITUSER = 301;
	public static final int WAITINFO = 302;
	public static final int ROOMUSER = 303;
	
	public static final int ERR_ALEADYUSER = 401;
	public static final int ERR_SERVERFULL = 402;
	public static final int ERR_ROOMSFULL = 403;
	public static final int ERR_ROOMFULL = 404;
	public static final int ERR_PASSWORD = 405;
	public static final int ERR_REJECTION = 406;
	public static final int ERR_NOUSER = 407;
	
}
