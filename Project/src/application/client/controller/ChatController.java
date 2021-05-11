package application.client.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ChatController implements Initializable {

	//---------------------------------------------------------------- 클라이언트 채팅방 UI 컴포넌트

	@FXML public TextArea fxChatArea;
	@FXML public TextField fxSendField;
	@FXML public ListView<String> fxChatUserList;
	@FXML public Button fxQuitBtn;
	@FXML public Button fxWhisperBtn;
	@FXML public Button fxCoerceOutBtn;
	@FXML public Button fxFileSendBtn;

	//---------------------------------------------------------------- 기타
	private ClientThread thread;
	public boolean isAdmin;
	private String idTo;
	private String id;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		fxChatArea.setEditable(false);
	}
	
	public void setThread(ClientThread thread) {
		this.thread = thread;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void resetComponents() { // 초기화 작업 메소드
		fxSendField.setText("");
		fxChatArea.setText("");
		fxSendField.requestFocus();
	}
	
	public void handleButtonAction(ActionEvent event) { // 채팅방 버튼 이벤트
		if(event.getSource().equals(fxWhisperBtn)) { // 귓속말 버튼
			fxSendField.setText("/w 상대아이디 message");
		}
		
		if(event.getSource().equals(fxCoerceOutBtn)) { // 강제퇴장 버튼
			idTo = fxChatUserList.getSelectionModel().getSelectedItem();
			if(!isAdmin) {
				thread.messageDialog("당신은 방장이 아닙니다.");
			}else if(idTo == null){
				thread.messageDialog("강제퇴장 ID를 선택하세요.");
			}else {
				thread.requestCoerseOut(idTo);
			}
		}
		
		if(event.getSource().equals(fxFileSendBtn)) { // 파일 전송 버튼
			String idTo = fxChatUserList.getSelectionModel().getSelectedItem();
			if(idTo == null){
				thread.messageDialog("상대방 아이디를 선택하세요.");
			}else if(id.equals(idTo)){
				thread.messageDialog("자기 자신을 선택하셨습니다.");
			}else {
				thread.requestSendFile(idTo);
			}
		}
		
		if(event.getSource().equals(fxQuitBtn)) { // 방 나가기 버튼
			thread.requestQuitRoom();
		}
	}// end 채팅방 버튼 이벤트
	
	public void handleKeyPress(KeyEvent key) { // 키 이벤트
		if(key.getSource().equals(fxSendField) && key.getCode() == KeyCode.ENTER) {
			String words = fxSendField.getText();
			String data;
			String idTo;
			String message = "";
			if(words.startsWith("/w")) {
				StringTokenizer st = new StringTokenizer(words, " ");
				String command = st.nextToken();
				try {
					idTo = st.nextToken();
					while(st.hasMoreTokens()) {
						data = st.nextToken();
						message = message + " " + data;
					}
				}catch (Exception e) {
					thread.messageDialog("아이디나 메시지를 입력해주세요.");
					return;
				}
				thread.requestSendWordTo(message, idTo);
				fxSendField.setText("");
			}else {
				thread.requestSendWord(words);
				fxSendField.requestFocus();
			}
		}
	}// end 키 이벤트
}
