package application.server.controller;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import application.Protocol;

public class WaitRoom {
	private static final int MAX_ROOM = 10; // 최대 방 개수
	private static final int MAX_USER = 10; // 최대 유저
	
	private static final String SEPARATOR = "-";
	private static final String DELIMETER = "'";
	private static final String DELIMETER2 = "=";
	
	private static Vector<String> userVector = new Vector<>(MAX_USER); // 유저 아이디
	static Vector<ChatRoom> roomVector = new Vector<>(MAX_ROOM); // 채팅방 목록
	private static Hashtable<String, ServerThread> userHash = new Hashtable<>(MAX_USER); // 유저 아이디 - 스레드
	private static Hashtable<Integer, ChatRoom> roomHash = new Hashtable<>(MAX_ROOM); // 방 번호 - 채팅방
	
	private static int userCount = 0;
	private static int roomCount = 0;
	
	public synchronized int addUser(String id, ServerThread client) { // 유저 추가
		if(userCount == MAX_USER) return Protocol.ERR_SERVERFULL;
		
		for(String str : userVector) {
			String tempId = str;
			if(tempId.equals(id)) return Protocol.ERR_ALEADYUSER;
		}
		
		for(ChatRoom room : roomVector) {
			ChatRoom tempRoom = room;
			if(tempRoom.checkUserID(id)) return Protocol.ERR_ALEADYUSER;
		}
		
		userVector.addElement(id);
		userHash.put(id, client);
		client.id = id;
		client.st_roomNumber = 0;
		return 0;
	}// end 유저 추가
	
	public synchronized void delUser(String id) { // 유저 삭제
		userVector.removeElement(id);
		userHash.remove(id);
		userCount--;
	}// end 유저 삭제
	
	public synchronized String getRooms() { // 방목록 얻기
		StringBuffer room = new StringBuffer();
		String rooms;
		int roomNum;
		Set<Integer> keySet = roomHash.keySet();
		Iterator<Integer> iterator = keySet.iterator();
		while(iterator.hasNext()) {
			roomNum = iterator.next();
			ChatRoom tempRoom = roomHash.get(roomNum);
			room.append(String.valueOf(roomNum));
			room.append(DELIMETER2);
			room.append(tempRoom.toString());
			room.append(DELIMETER);
		}
		
		try {
			rooms = new String(room);
			rooms = rooms.substring(0, rooms.length() - 1);
		}catch (Exception e) {
			return "empty";
		}
		return rooms;
	}// end 방목록 얻기
	
	public synchronized String getUsers() { // 유저 목록 얻기
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
	
	public synchronized int addRoom(ChatRoom room) { // 방 추가
		if(roomCount == MAX_ROOM) return Protocol.ERR_ROOMSFULL;
		
		roomVector.addElement(room);
		roomHash.put(ChatRoom.roomNumber, room);
		roomCount++;
		return 0;
	}// end 방 추가
	
	public String getWaitRoomInfo() { // 대기실 정보
		StringBuffer roomInfo = new StringBuffer();
		roomInfo.append(getRooms());
		roomInfo.append(SEPARATOR);
		roomInfo.append(getUsers());
		return roomInfo.toString();
	}// end 대기실 정보
	
	public synchronized int joinRoom(String id, ServerThread client, int roomNumber, String password) { // 채팅방 입장
		Integer roomNum = new Integer(roomNumber);
		ChatRoom room = roomHash.get(roomNum);
		if(room.isRock()) {
			if(room.checkPassword(password)) {
				if(!room.addUser(id, client)) {
					return Protocol.ERR_ROOMFULL;
				}
			}else {
				return Protocol.ERR_PASSWORD;
			}
		}else if(!room.addUser(id, client)) {
			return Protocol.ERR_ROOMFULL;
		}
		userVector.removeElement(id);
		userHash.remove(id);
		return 0;
	}// end 채팅방 입장
	
	public String getRoomInfo(int roomNumber) { // 방 정보
		Integer roomNum = new Integer(roomNumber);
		ChatRoom room = roomHash.get(roomNum);
		return room.getUsers();
	}// end 방 정보
	
	public synchronized boolean quitRoom(String id, ServerThread client, int roomNumber) { // 방 나가기
		boolean returnValue = false;
		Integer roomNum = new Integer(roomNumber);
		ChatRoom room = roomHash.get(roomNum);
		if(room.delUser(id)) {
			roomVector.removeElement(room);
			roomHash.remove(roomNum);
			roomCount--;
			returnValue = true;
		}
		userVector.addElement(id);
		userHash.put(id, client);
		return returnValue;
	}// end 방 나가기
	
	public synchronized Hashtable<String, ServerThread> getClients(int roomNumber) { // 유저 스레드 얻기 
		if(roomNumber == 0) return userHash;
		Integer roomNum = new Integer(roomNumber);
		ChatRoom room = roomHash.get(roomNum);
		return room.getClients();
	} // end 유저 스레드
}
