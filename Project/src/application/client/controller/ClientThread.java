package application.client.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import application.Protocol;
import application.client.file.ReceiveFile;
import application.client.file.SendFile;
import application.db.dao.ClientDao;
import application.db.vo.ClientVO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class ClientThread extends Thread {

	private Stage primaryStage;
	private WaitController waitRoom;
	private ChatController chatRoom;
	private LoginController lcontroller;
	
	private Socket socket;
	private BufferedOutputStream bos;
	private BufferedInputStream bis;
	private DataInputStream dis;
	private DataOutputStream dos;
	private StringBuffer sb;
	private String id;
	private Thread thisThread;
	private int ct_roomNumber;
	private boolean isRemember;
	private int value = -1;
	
	public ClientDao dao;
	private Stage fileDialog;
	private Stage dialog;
	
	private static final String SEPARATOR = "-";
	private static final String DELIMETER = "'";
	private static final String DELIMETER2 = "=";
	
	public void start(String ip, int port) { // 클라이언트 시작 메소드
		try {
			socket = new Socket(ip, port);
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());
			dis = new DataInputStream(bis);
			dos = new DataOutputStream(bos);
			sb = new StringBuffer();
			thisThread = this;
			dao = ClientDao.getInstance();
			this.start();
		}catch (Exception e) {
			Platform.runLater(() -> {
				lcontroller.getFxCheckLbl().setText("현재 서버에 접속할 수 없습니다.");
			});
		}
	}// end 클라이언트 시작 메소드
	
	// setter
	public void setController(LoginController lcontroller) { 
		this.lcontroller = lcontroller;
	}
	
	public void setRemember(boolean isRemember) {
		this.isRemember = isRemember;
	}
	
	public ClientVO getClientInfo() { // vo getter
		ClientVO client = dao.getUserInfo(id);
		return client;
	}
	
	public void changePassword(String pw) { // 비밀번호 변경 요청
		dao.changePassword(id, pw);
	}
	
	public void setPrimaryStage(Stage primaryStage) { // X버튼 이벤트 등록
		this.primaryStage = primaryStage;
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if(!(socket == null) && !socket.isClosed()) {
					requestLogout();
				}
			}
		});
	}// end X버튼 이벤트
	
	public void requestLogin(String id) { // 서버에 로그인 요청보내기
		this.id = id;
		sb.setLength(0);
		sb.append(Protocol.LOGIN);
		sb.append(SEPARATOR);
		sb.append(id);
		try {
			send(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}//end 서버에 로그인 요청보내기

	private void send(String sendData) throws IOException { // 데이터 전송 메소드
		if(dos != null ) {
			dos.writeUTF(sendData);
			dos.flush();
		}
	}// end 데이터 전송
	
	public void requestLogout() { // 서버에 로그아웃 요청 보내기
		try {
			sb.setLength(0);
			sb.append(Protocol.LOGOUT);
			sb.append(SEPARATOR);
			sb.append(id);
			send(sb.toString());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}// end 서버에 로그아웃 요청 보내기
	
	public void requestCreateRoom(String roomName, int roomMaxUser, int isRock, String password) { // 방 개설 요청 보내기
		try {
			sb.setLength(0);
			sb.append(Protocol.CREATE_ROOM);
			sb.append(SEPARATOR);
			sb.append(id);
			sb.append(SEPARATOR);
			sb.append(roomName);
			sb.append(DELIMETER);
			sb.append(roomMaxUser);
			sb.append(DELIMETER);
			sb.append(isRock);
			sb.append(DELIMETER);
			sb.append(password);
			send(sb.toString());
		}catch (Exception e) {
		}
	}// end 방 개설 요청 보내기
	
	public void requestEnterRoom(int roomNumber, String password) { // 방 입장 요청 보내기
		try {
			sb.setLength(0);
			sb.append(Protocol.ENTER_ROOM);
			sb.append(SEPARATOR);
			sb.append(id);
			sb.append(SEPARATOR);
			sb.append(roomNumber);
			sb.append(SEPARATOR);
			sb.append(password);
			send(sb.toString());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}// end 방 개설 요청 보내기
	
	public void requestQuitRoom() { // 방 나가기 요청 보내기
		try {
			sb.setLength(0);
			sb.append(Protocol.QUIT_ROOM);
			sb.append(SEPARATOR);
			sb.append(id);
			sb.append(SEPARATOR);
			sb.append(ct_roomNumber);
			send(sb.toString());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}// end 방 나가기 요청
	
	public void requestSendWord(String data) { // 메시지 전송 요청 보내기
		try {
			sb.setLength(0);
			sb.append(Protocol.SEND_WORD);
			sb.append(SEPARATOR);
			sb.append(id);
			sb.append(SEPARATOR);
			sb.append(ct_roomNumber);
			sb.append(SEPARATOR);
			sb.append(data);
			send(sb.toString());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}// end 메시지 전송 요청
	
	public void requestSendWordTo(String data, String idTo) { // 귓속말 요청
		try {
			sb.setLength(0);
			sb.append(Protocol.SEND_WORD_TO);
			sb.append(SEPARATOR);
			sb.append(id);
			sb.append(SEPARATOR);
			sb.append(ct_roomNumber);
			sb.append(SEPARATOR);
			sb.append(idTo);
			sb.append(SEPARATOR);
			sb.append(data);
			send(sb.toString());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}// end 귓속말 요청
	
	public void requestCoerseOut(String idTo) { // 강제 퇴장 요청
		try {
			sb.setLength(0);
			sb.append(Protocol.COERCE_OUT);
			sb.append(SEPARATOR);
			sb.append(ct_roomNumber);
			sb.append(SEPARATOR);
			sb.append(idTo);
			send(sb.toString());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}//end 강제퇴장 요청
	
	public void requestSendFile(String idTo) { // 파일 전송 요청
		// 전송 승인대기 다이알로그 띄우기
		Platform.runLater(() -> fileAcceptWait("상대방의 승인을 기다립니다."));
		try {
			sb.setLength(0);
			sb.append(Protocol.SEND_FILE);
			sb.append(SEPARATOR);
			sb.append(id);
			sb.append(SEPARATOR);
			sb.append(ct_roomNumber);
			sb.append(SEPARATOR);
			sb.append(idTo);
			send(sb.toString());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}//end 파일 전송 요청
	
	public void sendFile(String id, int roomNumber) { // 파일 전송 수락 및 취소 요청
		try {
			if(value == 1 || value == -1) {
				sb.setLength(0);
				sb.append(Protocol.NO_SEND_FILE);
				sb.append(SEPARATOR);
				sb.append(this.id);
				sb.append(SEPARATOR);
				sb.append(roomNumber);
				sb.append(SEPARATOR);
				sb.append(id);
				send(sb.toString());
			}else {
				StringTokenizer addr = new StringTokenizer(InetAddress.getLocalHost().toString(), "/");
				String hostname = "";
				String hostaddr = "";
				
				hostname = addr.nextToken();
				try {
					hostaddr = addr.nextToken();
				}catch (NoSuchElementException e) {
					hostaddr = hostname;
				}
				sb.setLength(0);
				sb.append(Protocol.OK_SEND_FILE);
				sb.append(SEPARATOR);
				sb.append(this.id);
				sb.append(SEPARATOR);
				sb.append(roomNumber);
				sb.append(SEPARATOR);
				sb.append(id);
				sb.append(SEPARATOR);
				sb.append(hostaddr);
				send(sb.toString());
				
				Stage primaryStage = (Stage) chatRoom.fxFileSendBtn.getScene().getWindow();
				Platform.runLater(() -> new ReceiveFile(primaryStage));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}// end 파일 전송 성공 및 실패 요청
	
	public void release() { // 자원 반납
		if(thisThread != null) thisThread = null;
		try {if(dis != null) dis.close();}catch (Exception e) {}finally {dis = null;}
		try {if(dos != null) dos.close();}catch (Exception e) {}finally {dos = null;}
		try {if(bis != null) bis.close();}catch (Exception e) {}finally {bis = null;}
		try {if(bos != null) bos.close();}catch (Exception e) {}finally {bos = null;}
		try {if(socket != null) socket.close();}catch (Exception e) {}finally {socket = null;}
	}// end 자원 반납

	@Override
	public void run() { // 쓰레드 시작
		try {
			Thread currThread = Thread.currentThread();
			while(currThread == thisThread) { 
				String receiveData = dis.readUTF();
				StringTokenizer st = new StringTokenizer(receiveData, SEPARATOR);
				int command = Integer.parseInt(st.nextToken());
				switch (command) {
					case Protocol.OK_LOGIN: { // 로그인 성공 시
						FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/waitRoom.fxml"));
						Parent parent = loader.load();
						Scene client = new Scene(parent);
						waitRoom = loader.getController();
						waitRoom.setThread(this);
						client.getStylesheets().add(getClass().getResource("../../css/room.css").toString());
						Platform.runLater(() -> {
							primaryStage.setScene(client);
							primaryStage.setTitle("WaitRoom");
						});
						ct_roomNumber = 0;
						try {
							StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
							Vector<String> roomInfo = new Vector<>();
							while(st1.hasMoreTokens()) {
								String temp = st1.nextToken();
								if(!temp.equals("empty")) {
									listRender(temp, roomInfo);
								}
							}
							// 대기실 방목록 수정
							Platform.runLater(() -> {
								waitRoom.fxRoomList.setItems(FXCollections.observableList(roomInfo));
								waitRoom.fxWaitSend.requestFocus();
							});
						}catch (Exception e) {
						}
						break;
					}
					
					case Protocol.NO_LOGIN: { // 로그인 실패
						int errCode = Integer.parseInt(st.nextToken());
						if(errCode == Protocol.ERR_ALEADYUSER) {
							Platform.runLater(() -> {
								lcontroller.getFxCheckLbl().setText("이미 존재하는 사용자입니다.");
							});
						}else if(errCode == Protocol.ERR_SERVERFULL) {
							Platform.runLater(() -> lcontroller.getFxCheckLbl().setText("현재 서버가 최대인원을 초과합니다."));
						}
						break;
					}
					
					case Protocol.WAITUSER: { // 대기실 사용자 목록 
						StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
						Vector<String> user = new Vector<>();
						while(st1.hasMoreTokens()) {
							user.addElement(st1.nextToken());
						}
						// 대기실 접속자 목록 수정
						Platform.runLater(() -> {
							waitRoom.fxWaitList.setItems(FXCollections.observableList(user));
							waitRoom.fxWaitSend.requestFocus();
						});
						break;
					}
					
					case Protocol.OK_LOGOUT: { // 로그아웃 요청
						FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/loginRoom.fxml"));
						Parent parent = loader.load();
						LoginController controller = loader.getController();
						controller.setStage(primaryStage);
						new animatefx.animation.FadeIn(parent).play();
						Scene scene = new Scene(parent);
						scene.getStylesheets().add(getClass().getResource("../../css/app.css").toString());
						Platform.runLater(() -> {
							if(isRemember) {
								String temp = dao.getUserInfo(id).getId();
								controller.setId(temp);
								controller.clrscr();
							}
							primaryStage.setScene(scene);
							primaryStage.setTitle("Login");
						});
						release();
						break;
					}
					
					case Protocol.OK_CREATE_ROOM: { // 방 개설 성공
						ct_roomNumber = Integer.parseInt(st.nextToken());
						FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/chatRoom.fxml"));
						Parent parent = loader.load();
						Scene client = new Scene(parent);
						client.getStylesheets().add(getClass().getResource("../../css/room.css").toString());
						Platform.runLater(() -> {
							primaryStage.setScene(client);
							primaryStage.setTitle("ChatRoom");
						});
						chatRoom = loader.getController();
						chatRoom.setThread(this);
						chatRoom.setId(id);
						chatRoom.isAdmin = true;
						if(chatRoom != null) {
							chatRoom.isAdmin = true;
							chatRoom.resetComponents();
						}
						break;
					}
					
					case Protocol.NO_CREATE_ROOM: { // 방 개설 실패
						int errCode = Integer.parseInt(st.nextToken());
						if(errCode == Protocol.ERR_ROOMSFULL) {
							Platform.runLater(() -> waitRoom.roomChkLbl.setText("더 이상 개설할 수 없습니다."));
						}
						break;
					}
					
					case Protocol.WAITINFO: { // 대기실 목록 수정
						StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
						StringTokenizer st2 = new StringTokenizer(st.nextToken(), DELIMETER);
						
						Vector<String> rooms = new Vector<>();
						Vector<String> users = new Vector<>();
						while(st1.hasMoreTokens()) {
							String temp = st1.nextToken();
							if(!temp.equals("empty")) {
								listRender(temp, rooms);
							}
						}
						Platform.runLater(() -> waitRoom.fxRoomList.setItems(FXCollections.observableList(rooms)));
						while(st2.hasMoreTokens()) {
							users.addElement(st2.nextToken());
						}
						Platform.runLater(() -> {
							waitRoom.fxWaitList.setItems(FXCollections.observableList(users));
							waitRoom.fxWaitSend.requestFocus();
						});
						break;
					}
					
					case Protocol.OK_ENTER_ROOM: { // 방 입장 성공
						ct_roomNumber = Integer.parseInt(st.nextToken());
						String id = st.nextToken();
						FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/chatRoom.fxml"));
						Parent parent = loader.load();
						Scene client = new Scene(parent);
						client.getStylesheets().add(getClass().getResource("../../css/room.css").toString());
						Platform.runLater(() -> {
							primaryStage.setScene(client);
							primaryStage.setTitle("ChatRoom");
						});
						chatRoom = loader.getController();
						chatRoom.setId(id);
						chatRoom.setThread(this);
						if(chatRoom != null) {
							chatRoom.resetComponents();
						}
						break;
					}
					
					case Protocol.NO_ENTER_ROOM: { // 방 입장 실패
						int errCode = Integer.parseInt(st.nextToken());
						if(errCode == Protocol.ERR_ROOMFULL) {
							Platform.runLater(() -> messageDialog("현재 대화방이 만원입니다."));
						}else if(errCode == Protocol.ERR_PASSWORD) {
							Platform.runLater(() -> messageDialog("비밀번호가 틀렸습니다."));
						}
						break;
					}
					
					case Protocol.ROOMUSER: { // 채팅방 목록 수정
						String id = st.nextToken();
						int code = Integer.parseInt(st.nextToken());
						
						StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
						Vector<String> user = new Vector<>();
						while(st1.hasMoreTokens()) {
							user.addElement(st1.nextToken());
						}
						Platform.runLater(() -> chatRoom.fxChatUserList.setItems(FXCollections.observableList(user)));
						if(code == 1) {
							Platform.runLater(() -> chatRoom.fxChatArea.appendText("### "+id+"님이 입장하셨습니다. ###\n"));
						}else if(code == 2) {
							Platform.runLater(() -> chatRoom.fxChatArea.appendText("### "+id+"님이 강제퇴장 되었습니다. ###\n"));
						}else {
							Platform.runLater(() -> chatRoom.fxChatArea.appendText("### "+id+"님이 퇴장하셨습니다. ###\n"));
						}
						Platform.runLater(() -> chatRoom.fxSendField.requestFocus());
						break;
					}
					
					case Protocol.OK_QUIT_ROOM: { // 방 나가기 성공
						String id = st.nextToken();
						if(chatRoom.isAdmin) {
							chatRoom.isAdmin = false;
						}
						FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/waitRoom.fxml"));
						Parent parent = loader.load();
						Scene client = new Scene(parent);
						waitRoom = loader.getController();
						waitRoom.setThread(this);
						client.getStylesheets().add(getClass().getResource("../../css/room.css").toString());
						Platform.runLater(() -> {
							primaryStage.setScene(client);
							primaryStage.setTitle("WaitRoom");
						});
						waitRoom.resetComponents();
						ct_roomNumber = 0;
						break;
					}
					
					case Protocol.OK_SEND_WORD: { // 메시지 전송 성공
						String id = st.nextToken();
						int roomNumber = Integer.parseInt(st.nextToken());
						try {
							String data = st.nextToken();
							if(roomNumber == 0) {
								waitRoom.fxWaitChatArea.appendText(id + " : " + data + "\n");
								if(id.equals(this.id)) {
									waitRoom.fxWaitSend.setText("");
								}
								waitRoom.fxWaitSend.requestFocus();
							}else {
								chatRoom.fxChatArea.appendText(id + " : " + data + "\n");
								if(id.equals(this.id)) {
									chatRoom.fxSendField.setText("");
								}
								chatRoom.fxSendField.requestFocus();
							}
						}catch(NoSuchElementException e) {
							if(roomNumber == 0) waitRoom.fxWaitSend.requestFocus();
							else chatRoom.fxSendField.requestFocus();
						}
						break;
					}
					
					case Protocol.OK_SEND_WORD_TO: { // 귓속말 성공
						String id = st.nextToken();
						String idTo = st.nextToken();
						int roomNumber = Integer.parseInt(st.nextToken());
						try {
							String data = st.nextToken();
							if(roomNumber == 0) {
								if(id.equals(this.id)) {
									waitRoom.fxWaitSend.setText("");
									waitRoom.fxWaitChatArea.appendText("<To:" + idTo + "> : " + data + "\n");
								}else {
									waitRoom.fxWaitChatArea.appendText("<From:" + id + "> : " + data + "\n");
								}
								waitRoom.fxWaitSend.requestFocus();
							}else {
								if(id.equals(this.id)) {
									chatRoom.fxSendField.setText("");
									chatRoom.fxChatArea.appendText("<To:" + idTo + "> : " + data + "\n");
								}else {
									chatRoom.fxChatArea.appendText("<From:" + id + "> : " + data + "\n");
								}
								chatRoom.fxSendField.requestFocus();
							}
						}catch(NoSuchElementException e) {
							if(roomNumber == 0) waitRoom.fxWaitSend.requestFocus();
							else chatRoom.fxSendField.requestFocus();
						}
						break;
					}
					
					case Protocol.NO_SEND_WORD_TO: { // 귓속말 실패
						String id = st.nextToken();
						int roomNumber = Integer.parseInt(st.nextToken());
						String message = "";
						Platform.runLater(() -> messageDialog("해당 회원님이 존재하지 않습니다."));
						break;
					}
					
					case Protocol.OK_COERCE_OUT: { // 강제퇴장 성공
						FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/waitRoom.fxml"));
						Parent parent = loader.load();
						Scene client = new Scene(parent);
						waitRoom = loader.getController();
						waitRoom.setThread(this);
						client.getStylesheets().add(getClass().getResource("../../css/room.css").toString());
						Platform.runLater(() -> {
							primaryStage.setScene(client);
							primaryStage.setTitle("WaitRoom");
						});
						waitRoom.resetComponents();
						ct_roomNumber = 0;
						waitRoom.fxWaitChatArea.appendText("### 방장에 의해 강제퇴장 되었습니다. ###\n");
						break;
					}
					
					case Protocol.SEND_FILE: { // 파일보내기 요청 수락여부 확인
						String id = st.nextToken();
						int roomNumber = Integer.parseInt(st.nextToken());
						Platform.runLater(() -> fileIsAccept(id + "님의 파일전송을 수락하시겠습니까?", id, roomNumber));
						break;
					}// end 파일 보내기 요청 수락 여부 
					
					case Protocol.OK_SEND_FILE: { // 파일 보내기 요청 성공
						String id = st.nextToken();
						String addr = st.nextToken();
						
						Platform.runLater(() -> fileDialog.close());
						// 파일 송신 클라이언트 실행
						Stage primaryStage = (Stage) chatRoom.fxFileSendBtn.getScene().getWindow();
						Platform.runLater(() -> new SendFile(addr, primaryStage));
						break;
					}
					
					case Protocol.NO_SEND_FILE: { // 파일 보내기 요청 실패
						int code = Integer.parseInt(st.nextToken());
						String id = st.nextToken();
						Platform.runLater(() -> fileDialog.close());
						if(code == Protocol.ERR_REJECTION) {
							Platform.runLater(() -> messageDialog(id + "님이 파일수신을 거부하였습니다."));
							break;
						}else if(code == Protocol.ERR_NOUSER) {
							Platform.runLater(() -> messageDialog(id + "님은 방에 존재하지 않습니다."));
							Platform.runLater(() -> fileDialog.close());
							break;
						}
					}
					
				}// end switch
				Thread.sleep(500);
			}// end while
		}catch (EOFException e) {
			try {
				Platform.runLater(() -> {
					Platform.runLater(() -> messageDialog("서버와의 연결이 끊어졌습니다."));
				});
				FXMLLoader cLoader = new FXMLLoader(getClass().getResource("../fxml/loginRoom.fxml"));
				Parent cRoot = cLoader.load();
				
				LoginController cController = cLoader.getController();
				cController.setStage(primaryStage);
				
				new animatefx.animation.FadeIn(cRoot).play();
				Scene scene = new Scene(cRoot);
				scene.getStylesheets().add(getClass().getResource("../../css/app.css").toString());
				Platform.runLater(() -> {
					primaryStage.setScene(scene);
					primaryStage.setTitle("Login");
				});
				release();
			} catch (IOException e1) {}
			
		}catch (InterruptedException e) {
			release();
		}catch (IOException e) {
			release();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}// end run
	
	
	public void messageDialog(String message) { // 확인 다이알로그
		Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("Message");
		Parent parent = null;
		Scene scene = null;
		try {
			parent = FXMLLoader.load(getClass().getResource("../fxml/dialog/confirmDialog.fxml"));
			scene = new Scene(parent);
			scene.getStylesheets().add(getClass().getResource("../../css/app.css").toString());
			Button confirmBtn = (Button) parent.lookup("#confirmBtn");
			Label confirmLbl = (Label) parent.lookup("#confirmLbl");
			
			confirmLbl.setText(message);
			confirmBtn.setOnAction(event -> dialog.close());
		} catch (IOException e) {}
		ClientThread.posDialog(dialog);
		dialog.setScene(scene);
		dialog.setResizable(false);
		dialog.show();
	}// end 확인 다이알로그
	
	public void fileIsAccept(String message, String id, int roomNumber) { // 파일 수락 다이알로그
		dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("FileConfirm");
		Parent parent = null;
		Scene scene = null;
		try {
			parent = FXMLLoader.load(getClass().getResource("../fxml/dialog/fileConfirm.fxml"));
			scene = new Scene(parent);
			scene.getStylesheets().add(getClass().getResource("../../css/app.css").toString());
			Button acceptBtn = (Button) parent.lookup("#acceptBtn");
			Button cancelBtn = (Button) parent.lookup("#cancelBtn");
			Label confirmLbl = (Label) parent.lookup("#confirmLbl");
			
			confirmLbl.setText(message);
			acceptBtn.setOnAction(event -> { // 수락 버튼
				value = 0;
				sendFile(id, roomNumber);
				dialog.close();
			});
			cancelBtn.setOnAction(event -> { // 취소 버튼
				value = 1;
				sendFile(id, roomNumber);
				dialog.close();
			});
			
			ClientThread.posDialog(dialog);
			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.show();
		} catch (IOException e) {}
	}// end 확인 다이알로그
	
	public void fileAcceptWait(String message) { // 파일 수락대기 다이알로그
		fileDialog = new Stage(StageStyle.UTILITY);
		fileDialog.initModality(Modality.WINDOW_MODAL);
		fileDialog.initOwner(primaryStage);
		fileDialog.setTitle("FileConfirm");
		Parent parent = null;
		Scene scene = null;
		try {
			parent = FXMLLoader.load(getClass().getResource("../fxml/dialog/confirmDialog.fxml"));
			scene = new Scene(parent);
			scene.getStylesheets().add(getClass().getResource("../../css/app.css").toString());
			Button confirmBtn = (Button) parent.lookup("#confirmBtn");
			Label confirmLbl = (Label) parent.lookup("#confirmLbl");
			
			confirmLbl.setText(message);
			confirmBtn.setOnAction(event -> fileDialog.close());
		} catch (IOException e) {}
		ClientThread.posDialog(fileDialog);
		fileDialog.setScene(scene);
		fileDialog.setResizable(false);
		fileDialog.show();
	}// end 확인 다이알로그
	
	
	public static void posDialog(Stage dialog) { // 다이알로그 위치 설정
        try {
            Window window = dialog.getOwner();
            double windowCenterX = window.getX() + (window.getWidth() / 2);
            double windowCenterY = window.getY() + (window.getHeight() / 2);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    dialog.setX(windowCenterX - (dialog.getWidth() / 2));
                    dialog.setY(windowCenterY - (dialog.getHeight() / 2));
                }
            });
        } catch (Throwable e) {
        	e.printStackTrace();
        }
    }// end 다이알로그 위치 설정
	
	public void listRender(String str, Vector<String> roomInfo) { // 방 리스트 목록 수정
		String[] temps = str.split(DELIMETER2);
		String temp = temps[0] + ".    " + temps[1] + "    [" + temps[2] + "/" + temps[3] + " (" + temps[4] + " - " + temps[5] + ")]";
		roomInfo.addElement(temp);
	}// end 방 리스트 목록 수정
	
} 
