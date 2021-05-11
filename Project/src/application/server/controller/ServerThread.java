package application.server.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import application.Protocol;
import javafx.application.Platform;
import javafx.collections.FXCollections;

public class ServerThread extends Thread {

	Socket socket;
	private ServerController controller;
	private BufferedOutputStream bos;
	private BufferedInputStream bis;
	private DataInputStream dis;
	private DataOutputStream dos;
	private StringBuffer sb;
	private WaitRoom waitRoom;
	
	public String id;
	public String nick;
	public int st_roomNumber;
	
	private static final String SEPARATOR = "-";
	private static final String DELIMETER = "'";
	private static final int WAITROOM = 0;
	
	private Date date = new Date();
	private SimpleDateFormat sdf = new SimpleDateFormat("YY/MM/DD a HH:mm:ss");

	// 생성자
	public ServerThread(Socket socket) {
		try {
			this.socket = socket;
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());
			dis = new DataInputStream(bis);
			dos = new DataOutputStream(bos);
			sb = new StringBuffer();
			waitRoom = new WaitRoom();
		} catch (IOException e) {
		}
	}
	
	public void setController(ServerController controller) { // ServerController Setter
		this.controller = controller;
	}

	public void sendErrCode(int message, int errCode) throws IOException { // 에러코드 전송
		sb.setLength(0);
		sb.append(message); // 에러 메시지 (NO_~)
		sb.append(SEPARATOR);
		sb.append(errCode); // 에러 코드 (ERR_~)
 		send(sb.toString());
	}// end 에러코드 전송

	public void modifyWaitRoom() throws IOException { // 대기실 대기방 수정
		sb.setLength(0);
		sb.append(Protocol.WAITINFO); // 프로토콜
		sb.append(SEPARATOR);
		sb.append(waitRoom.getWaitRoomInfo()); // 대기실 정보
		broadcast(sb.toString(), WAITROOM);
	}// end 대기실 대기방 수정

	public void modifyWaitUser() throws IOException { // 대기실 인원 수정
		String users = waitRoom.getUsers();
		sb.setLength(0);
		sb.append(Protocol.WAITUSER); // 프로토콜
		sb.append(SEPARATOR);
		sb.append(users); // 대기실 유저 목록
		broadcast(sb.toString(), WAITROOM);
	}// end 대기실 인원 수정

	public void modifyRoomUser(int roomNumber, String id, int code) throws IOException { // 채팅방 인원 수정
		String users = waitRoom.getRoomInfo(roomNumber);
		sb.setLength(0);
		sb.append(Protocol.ROOMUSER); // 프로토콜
		sb.append(SEPARATOR);
		sb.append(id); // 방에 들어가는 유저 아이디
		sb.append(SEPARATOR);
		sb.append(code); // 약속코드 (1은 입장, 0은 나가기, 2는 강제퇴장)
		sb.append(SEPARATOR);
		sb.append(users); // 방안 유저 목록
		broadcast(sb.toString(), roomNumber);
	}// end 채팅방 인원 수정

	public synchronized void send(String sendData) throws IOException { // 일반 전송 메소드
		dos.writeUTF(sendData);
		dos.flush();
	}// end 일반 전송 메소드

	public synchronized void broadcast(String sendData, int roomNumber) throws IOException { // 다중 전송 메소드
		ServerThread client;
		Hashtable<String, ServerThread> clients = waitRoom.getClients(roomNumber);
		Set<String> keySet = clients.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			client = clients.get(iterator.next());
			client.send(sendData);
		}
	}// end 다중 전송 메소드

	@Override
	public void run() { // 서버쓰레드 시작
		try {
			while(true) {
				String receiveData = dis.readUTF();
				StringTokenizer st = new StringTokenizer(receiveData, SEPARATOR);
				int command = Integer.parseInt(st.nextToken());
				switch (command) {
					case Protocol.LOGIN: { // 로그인 처리
						st_roomNumber = WAITROOM;
						int result;
						id = st.nextToken();
						result = waitRoom.addUser(id, this);
						sb.setLength(0);
						if (result == 0) {
							ServerController.clients.add(this);
							ServerController.ids.add(id);
							sb.append(Protocol.OK_LOGIN);
							sb.append(SEPARATOR);
							sb.append(waitRoom.getRooms());
							send(sb.toString());
							Platform.runLater(() -> {
								controller.getLogArea().appendText("[" + sdf.format(date) + "] - " + id + "회원이 입장하였습니다.\n");
								controller.getConnectArea().setItems(FXCollections.observableList(ServerController.ids));
							});
							modifyWaitUser();
						} else {
							sendErrCode(Protocol.NO_LOGIN, result);
						}
						break;
					}
					
					case Protocol.CREATE_ROOM: { // 방 개설 처리
						String id, roomName, password;
						int roomMaxUser, result;
						boolean isRock;
						id = st.nextToken();
						String roomInfo = st.nextToken();
						StringTokenizer room = new StringTokenizer(roomInfo, DELIMETER);
						roomName = room.nextToken();
						roomMaxUser = Integer.parseInt(room.nextToken());
						isRock = (Integer.parseInt(room.nextToken()) == 0) ? false : true;
						password = room.nextToken();
						
						ChatRoom chatRoom = new ChatRoom(roomName, roomMaxUser, isRock, password, id);
						result = waitRoom.addRoom(chatRoom);
						if(result == 0) {
							st_roomNumber = ChatRoom.getRoomNumber();
							boolean temp = chatRoom.addUser(id, this);
							waitRoom.delUser(id);
							
							sb.setLength(0);
							sb.append(Protocol.OK_CREATE_ROOM);
							sb.append(SEPARATOR);
							sb.append(st_roomNumber);
							send(sb.toString());
							modifyWaitRoom();
							modifyRoomUser(st_roomNumber, id, 1);
						}else {
							sendErrCode(Protocol.NO_CREATE_ROOM, result);
						}
						break;
					}
					
					case Protocol.ENTER_ROOM: { // 방 입장 처리
						String id;
						String password;
						int roomNumber;
						int result;
						id = st.nextToken();
						roomNumber = Integer.parseInt(st.nextToken());
						try {
							password = st.nextToken();
						}catch (Exception e) {
							password = "0";
						}
						result = waitRoom.joinRoom(id, this, roomNumber, password);
						if(result == 0) {
							sb.setLength(0);
							sb.append(Protocol.OK_ENTER_ROOM);
							sb.append(SEPARATOR);
							sb.append(roomNumber);
							sb.append(SEPARATOR);
							sb.append(id);
							st_roomNumber = roomNumber;
							send(sb.toString());
							modifyRoomUser(roomNumber, id, 1);
							modifyWaitRoom();
						}else {
							sendErrCode(Protocol.NO_ENTER_ROOM, result);
						}
						break;
					}
					
					case Protocol.QUIT_ROOM: { // 방 나가기 처리
						String id = st.nextToken();
						int roomNumber;
						boolean updateWaitInfo;
						roomNumber = Integer.parseInt(st.nextToken());
						
						updateWaitInfo = waitRoom.quitRoom(id, this, roomNumber);
						
						sb.setLength(0);
						sb.append(Protocol.OK_QUIT_ROOM);
						sb.append(SEPARATOR);
						sb.append(id);
						send(sb.toString());
						st_roomNumber = WAITROOM;
						
						if(updateWaitInfo) {
							modifyWaitRoom();
						}else {
							modifyWaitRoom();
							modifyRoomUser(roomNumber, id, 0);
						}
						break;
					}
					
					case Protocol.LOGOUT: { // 로그아웃 처리
						String id = st.nextToken();
						waitRoom.delUser(id);
						ServerController.clients.remove(this);
						ServerController.ids.remove(id);
						sb.setLength(0);
						sb.append(Protocol.OK_LOGOUT);
						send(sb.toString());
						modifyWaitUser();
						Platform.runLater(() -> {
							controller.getLogArea().appendText("[" + sdf.format(date) + "] - " + id + "회원이 퇴장하였습니다.\n");
							controller.getConnectArea().setItems(FXCollections.observableList(ServerController.ids));
						});
						release();
						break;
					}
					
					case Protocol.SEND_WORD: { // 메세지 보내기
						String id = st.nextToken();
						int roomNumber = Integer.parseInt(st.nextToken());
						sb.setLength(0);
						sb.append(Protocol.OK_SEND_WORD);
						sb.append(SEPARATOR);
						sb.append(id);
						sb.append(SEPARATOR);
						sb.append(st_roomNumber);
						sb.append(SEPARATOR);
						try {
							String data = st.nextToken();
							sb.append(data);
						}catch (Exception e) {}
						
						broadcast(sb.toString(), roomNumber);
						
						break;
					}
					
					case Protocol.SEND_WORD_TO: { // 귓속말 보내기
						String id = st.nextToken();
						int roomNumber = Integer.parseInt(st.nextToken());
						String idTo = st.nextToken();
						Hashtable<String, ServerThread> room = waitRoom.getClients(roomNumber);
						ServerThread client = null;
						
						if((client = room.get(idTo)) != null) {
							sb.setLength(0);
							sb.append(Protocol.OK_SEND_WORD_TO);
							sb.append(SEPARATOR);
							sb.append(id);
							sb.append(SEPARATOR);
							sb.append(idTo);
							sb.append(SEPARATOR);
							sb.append(st_roomNumber);
							sb.append(SEPARATOR);
							try {
								String data = st.nextToken();
								sb.append(data);
							}catch (Exception e) {}
							
							client.send(sb.toString());
							send(sb.toString());
							break;
						}else {
							sb.setLength(0);
							sb.append(Protocol.NO_SEND_WORD_TO);
							sb.append(SEPARATOR);
							sb.append(idTo);
							sb.append(SEPARATOR);
							sb.append(st_roomNumber);
							send(sb.toString());
							break;
						}
					}
					
					case Protocol.SEND_FILE: { // 파일 보내기
						String id = st.nextToken();
						int roomNumber = Integer.parseInt(st.nextToken());
						String idTo = st.nextToken();
						Hashtable<String, ServerThread> room = waitRoom.getClients(roomNumber);
						ServerThread client = null;
						if((client = (ServerThread) room.get(idTo)) != null) {
							sb.setLength(0);
							sb.append(Protocol.SEND_FILE);
							sb.append(SEPARATOR);
							sb.append(id);
							sb.append(SEPARATOR);
							sb.append(st_roomNumber);
							client.send(sb.toString());
							break;
						}else {
							sb.setLength(0);
							sb.append(Protocol.NO_SEND_FILE);
							sb.append(SEPARATOR);
							sb.append(Protocol.ERR_NOUSER);
							sb.append(SEPARATOR);
							sb.append(idTo);
							send(sb.toString());
							break;
						}
					}
					
					case Protocol.NO_SEND_FILE: { // 파일 전송실패
						String id = st.nextToken();
						int roomNumber = Integer.parseInt(st.nextToken());
						String idTo = st.nextToken();
						
						Hashtable<String, ServerThread> room = waitRoom.getClients(roomNumber);
						ServerThread client = room.get(idTo);
						sb.setLength(0);
						sb.append(Protocol.NO_SEND_FILE);
						sb.append(SEPARATOR);
						sb.append(Protocol.ERR_REJECTION);
						sb.append(SEPARATOR);
						sb.append(id);
						client.send(sb.toString());
						break;
					}
					
					case Protocol.OK_SEND_FILE: { // 파일 전송성공
						String id = st.nextToken();
						int roomNumber = Integer.parseInt(st.nextToken());
						String idTo = st.nextToken();
						String hostaddr = st.nextToken();
						
						Hashtable<String, ServerThread> room = waitRoom.getClients(roomNumber);
						ServerThread client = room.get(idTo);
						sb.setLength(0);
						sb.append(Protocol.OK_SEND_FILE);
						sb.append(SEPARATOR);
						sb.append(id);
						sb.append(SEPARATOR);
						sb.append(hostaddr);
						client.send(sb.toString());
						break;
					}
					
					case Protocol.COERCE_OUT: { // 강제퇴장
						int roomNumber = Integer.parseInt(st.nextToken());
						String idTo = st.nextToken();
						Hashtable<String, ServerThread> room = waitRoom.getClients(roomNumber);
						ServerThread client = room.get(idTo);
						boolean updateWaitInfo = waitRoom.quitRoom(idTo, client, roomNumber);
						
						sb.setLength(0);
						sb.append(Protocol.OK_COERCE_OUT);
						sb.append(SEPARATOR);
						client.send(sb.toString());
						client.st_roomNumber = 0;
						if(updateWaitInfo) {
							modifyWaitRoom();
						}else {
							modifyWaitRoom();
							modifyRoomUser(roomNumber, idTo, 2);
						}
						break;
					}
				}// end switch
				Thread.sleep(500);
			}// end while
		} catch (NullPointerException e) {
		} catch (InterruptedException e) {
			remove(this);
		} catch (EOFException e) {
		} catch (IOException e) {
			remove(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// end run
	
	private void remove(ServerThread client) { // 유저 제거
		if(st_roomNumber == 0) {
			waitRoom.delUser(id);
		}else {
			boolean temp = waitRoom.quitRoom(id, this, st_roomNumber);
			waitRoom.delUser(id);
		}
		release();
	}// end 유저 제거

	private void release() { // 자원 닫기
		try { if(dis != null) dis.close(); }catch (Exception e) {} finally {dis = null;}
		try { if(dos != null) dos.close(); }catch (Exception e) {} finally {dos = null;}
		try {if(bis != null) bis.close();}catch (Exception e) {}finally {bis = null;}
		try {if(bos != null) bos.close();}catch (Exception e) {}finally {bos = null;}
		try { if(socket != null) socket.close(); }catch (Exception e) {} finally {socket = null;}
		if(id != null) {id = null;}
	}// end 자원 닫기
}
