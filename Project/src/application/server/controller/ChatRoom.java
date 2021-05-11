package application.server.controller;

import java.util.Hashtable;
import java.util.Vector;

public class ChatRoom {
	
	private static final String DELIMETER = "'";
	private static final String DELIMETER2 = "=";
	
	Vector<String> userVector;
	private Hashtable<String, ServerThread> userHash;
	
	public static int roomNumber = 0;
	private String roomName;
	private int roomMaxUser;
	private int roomUser;
	private boolean isRock;
	private String password;
	private String admin;
	
	// 생성자
	public ChatRoom(String roomName, int roomMaxUser, boolean isRock, String password, String admin) {
		roomNumber++;
		this.roomName = roomName;
		this.roomMaxUser = roomMaxUser;
		this.roomUser = 0;
		this.isRock = isRock;
		this.password = password;
		this.admin = admin;
		this.userVector = new Vector<>(roomMaxUser);
		this.userHash = new Hashtable<>(roomMaxUser);
	}
	
	public boolean addUser(String id, ServerThread client) { // 채팅방 인원 추가
		if(roomUser == roomMaxUser) {
			return false;
		}
		
		userVector.addElement(id);
		userHash.put(id, client);
		roomUser++;
		return true;
	}// end 채팅방 인원 추가
	
	public boolean checkPassword(String passwd) { // 비밀번호 확인
		return password.equals(passwd);
	}// end 비밀번호 확인
	
	public boolean checkUserID(String id) { // 아이디 확인
		for(String str : userVector) {
			String tempId = str;
			if(tempId.equals(id)) return true;
		}
		return false;
	}// end 아이디 확인
	
	public boolean isRock() { 
		return isRock;
	}
	
	public boolean delUser(String id) { // 유저 제거
		userVector.removeElement(id);
		userHash.remove(id);
		roomUser--;
		return userVector.isEmpty();
	}// end 유저 제거
	
	public synchronized String getUsers() { // 유저 목록 
		StringBuffer id = new StringBuffer();
		String ids;
		for(String str : userVector) {
			id.append(str);
			id.append(DELIMETER);
		}
		try {
			ids = new String(id);
			ids = ids.substring(0, ids.length() - 1);
		}catch(StringIndexOutOfBoundsException e) {
			return "";
		}
		return ids;
	}// end 유저 목록
	
	public ServerThread getUser(String id) { // 특정 유저
		ServerThread client = userHash.get(id);
		return client;
	}// end 특정 유저
	
	public Hashtable<String, ServerThread> getClients(){ // 유저 스레드 얻기
		return userHash;
	}// end 유저 스레드
	
	public static synchronized int getRoomNumber() { // 방 번호 얻기
		return roomNumber;
	}// end 방 번호
	
	@Override
	public String toString() { 
		StringBuffer room = new StringBuffer();
		room.append(roomName);
		room.append(DELIMETER2);
		room.append(String.valueOf(roomUser));
		room.append(DELIMETER2);
		room.append(String.valueOf(roomMaxUser));
		room.append(DELIMETER2);
		
		if(isRock) {
			room.append("비공개");
		}else {
			room.append("공개");
		}
		
		room.append(DELIMETER2);
		room.append(admin);
		return room.toString(); // 채팅방 정보
	}
}
