package application.client.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import application.db.vo.ClientVO;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WaitController implements Initializable {

	// ---------------------------------------------------------------- 클라이언트 대기실 UI
	@FXML
	private Button fxMakeRoomBtn;
	@FXML
	private Button fxWhisperBtn;
	@FXML
	private Button fxMyInfoBtn;
	@FXML
	private Button fxLogoutBtn;

	@FXML
	public ListView<String> fxRoomList;
	@FXML
	public ListView<String> fxWaitList;

	@FXML
	public TextField fxWaitSend;
	@FXML
	public TextArea fxWaitChatArea;

	public Label roomChkLbl;
	public Label pwChkLbl;

	// ---------------------------------------------------------------- 기타
	private ClientThread thread;
	private String roomName;
	private String roomPassword;
	private int isRock = 0;
	private int roomMaxUser;

	private int roomNumber;
	private String password;
	private boolean doRock;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		fxWaitChatArea.setEditable(false);
	}

	public void setThread(ClientThread thread) {
		this.thread = thread;
		this.password = "0";
	}

	public void handleListAction(MouseEvent event) { // 리스트 이벤트
		if (event.getClickCount() == 2) {
			String select = fxRoomList.getSelectionModel().getSelectedItem();
			setSelectedRoomInfo(select);
			if (doRock && password.equals("0")) {
				enterRoomDialog();
			} else {
				thread.requestEnterRoom(roomNumber, password);
			}
		}
	}// end 리스트 이벤트

	private void setSelectedRoomInfo(String select) { // 방 정보 세팅
		roomNumber = Integer.parseInt(select.substring(0, 1));
		String roomName = select.substring(2, select.indexOf("[")).trim();
		int maxUser = Integer.parseInt(select.substring(select.indexOf("/") + 1, select.indexOf("(")).trim());
		int user = Integer.parseInt(select.substring(select.indexOf("[") + 1, select.indexOf("/")));
		doRock = select.substring(select.indexOf("(") + 1, select.indexOf("-")).trim().equals("비공개") ? true : false;
	}// end 방정보 세팅

	public void handleButtonAction(ActionEvent event) { // 채팅방 버튼 이벤트

		if (event.getSource().equals(fxMakeRoomBtn)) { // 대화방 개설 버튼 클릭
			setMakeRoomDialog();
		} 

		if (event.getSource().equals(fxMyInfoBtn)) { // 내 정보 버튼 클릭
			updateDialog();
		} 

		if (event.getSource().equals(fxLogoutBtn)) { // 로그아웃 버튼 클릭
			setLogoutDialog();
		}

		if (event.getSource().equals(fxWhisperBtn)) { // 귓속말 버튼 클릭
			fxWaitSend.setText("/w 상대아이디 message");
		}

	}// end 채팅방 버튼 이벤트

	public void handleKeyPress(KeyEvent key) { // 키 이벤트
		if (key.getSource().equals(fxWaitSend) && key.getCode() == KeyCode.ENTER) {
			String words = fxWaitSend.getText();
			String data;
			String idTo;
			String message = "";
			if (words.startsWith("/w")) {
				StringTokenizer st = new StringTokenizer(words, " ");
				String command = st.nextToken();
				try {
					idTo = st.nextToken();
					while (st.hasMoreTokens()) {
						data = st.nextToken();
						message = message + " " + data;
					}
				} catch (Exception e) {
					Platform.runLater(() -> thread.messageDialog("아이디나 메시지를 입력해주세요."));
					return;
				}
				thread.requestSendWordTo(message, idTo);
				fxWaitSend.setText("");
			} else {
				thread.requestSendWord(words);
				fxWaitSend.requestFocus();
			}
		}
	}// end 키 이벤트

	public void setMakeRoomDialog() { // 대기실 방개설 다이알로그 생성
		try {
			Stage primaryStage = (Stage) fxMakeRoomBtn.getScene().getWindow();
			Stage dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(primaryStage);
			dialog.setTitle("MakeRoom");

			FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/dialog/makeRoom.fxml"));
			Parent parent = loader.load();
			Scene scene = new Scene(parent);

			scene.getStylesheets().add(getClass().getResource("../../css/room.css").toString());
			Button closeBtn = (Button) parent.lookup("#closeBtn");
			closeBtn.setOnAction(event -> dialog.close());

			Button makeRoomBtn = (Button) parent.lookup("#makeRoomBtn");
			TextField roomNameTf = (TextField) parent.lookup("#roomName");
			TextField countTf = (TextField) parent.lookup("#count");
			TextField roomPwTf = (TextField) parent.lookup("#roomPw");
			roomChkLbl = (Label) parent.lookup("#roomChkLbl");
			ToggleGroup tg = new ToggleGroup();
			RadioButton rock = (RadioButton) parent.lookup("#rock");
			RadioButton unrock = (RadioButton) parent.lookup("#unrock");
			rock.setToggleGroup(tg);
			unrock.setToggleGroup(tg);
			unrock.setSelected(true);
			roomPwTf.setEditable(false);
			tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
				@Override
				public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
					if (unrock.isSelected()) {
						roomPwTf.setText("");
						roomPwTf.setEditable(false);
					} else {
						roomPwTf.setEditable(true);
					}
				}
			});
			makeRoomBtn.setOnAction(event -> { // 해당 방 조건에 맞게 방 생성하는 로직 필요
				roomName = roomNameTf.getText();
				if (roomName.equals("") || roomName == null || roomName.length() < 2) {
					roomChkLbl.setText("방 제목이 올바르지 않습니다(최소 2글자)");
					return;
				}

				roomMaxUser = 0;
				if (countTf.getText().equals("") || countTf.getText() == null || !countTf.getText().matches("\\d")) {
					roomChkLbl.setText("인원수가 올바르지 않습니다.");
					return;
				} else {
					roomMaxUser = Integer.parseInt(countTf.getText());
					if (roomMaxUser < 2 || roomMaxUser > 8) {
						roomChkLbl.setText("가능한 인원수는 2~8명 입니다.");
						return;
					}
				}

				if (rock.isSelected()) {
					isRock = 1;
				}

				roomPassword = roomPwTf.getText();
				if (isRock == 1) {
					if (roomPassword.equals("") || roomPassword == null || roomPassword.length() < 4) {
						roomChkLbl.setText("비밀번호가 올바르지 않습니다(최소 4글자)");
						return;
					}
				} else {
					roomPassword = "0";
				}
				thread.requestCreateRoom(roomName, roomMaxUser, isRock, roomPassword);
				dialog.close();
			});
			ClientThread.posDialog(dialog);
			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.show();
		} catch (IOException e) {
		}
	}// end 대기실 방개설 다이알로그 생성

	public void updateDialog() { // 대기실 수정 다이알로그 생성
		try {
			Stage primaryStage = (Stage) fxMyInfoBtn.getScene().getWindow();
			Stage dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(primaryStage);
			dialog.setTitle("MyInfo");

			FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/dialog/chatMyInfo.fxml"));
			Parent parent = loader.load();
			Scene scene = new Scene(parent);

			scene.getStylesheets().add(getClass().getResource("../../css/room.css").toString());
			Button closeBtn = (Button) parent.lookup("#closeBtn");
			Button updateBtn = (Button) parent.lookup("#updateBtn");
			closeBtn.setOnAction(event -> dialog.close());

			Label confirmLbl = (Label) parent.lookup("#confirmLbl");
			ClientVO client = thread.getClientInfo();
			TextField pwLbl = (TextField) parent.lookup("#pwLbl");
			TextField pwchkLbl = (TextField) parent.lookup("#pwchkLbl");
			TextField emailLbl = (TextField) parent.lookup("#emailLbl");
			emailLbl.setText(client.getEmail());
			
			updateBtn.setOnAction(event -> {
				client.setEmail(emailLbl.getText());
				client.setPassword(pwLbl.getText());
				if (pwchkLbl.getText().equals("") && client.getPassword().equals("")) {
					thread.dao.updateUser(client);
					confirmLbl.setTextFill(Color.BLUE);
					confirmLbl.setText("이메일 수정이 완료되었습니다.");
				} else if(client.confirmPassword(pwchkLbl.getText()) && emailLbl.getText().equals("")){
					thread.changePassword(pwLbl.getText());
					confirmLbl.setTextFill(Color.BLUE);
					confirmLbl.setText("비밀번호 수정이 완료되었습니다.");
				} else if(client.confirmPassword(pwchkLbl.getText())){
					thread.changePassword(pwLbl.getText());
					thread.dao.updateUser(client);
					confirmLbl.setTextFill(Color.BLUE);
					confirmLbl.setText("수정이 완료되었습니다.");
				} else {
					confirmLbl.setTextFill(Color.RED);
					confirmLbl.setText("비밀번호를 다시 확인해주세요.");
				}
			});

			ClientThread.posDialog(dialog);
			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// end 대기실 수정 다이알로그 생성

	public void setLogoutDialog() { // 로그아웃 확인 다이알로그
		try {
			Stage primaryStage = (Stage) fxLogoutBtn.getScene().getWindow();
			Stage dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(primaryStage);
			dialog.setTitle("isLogout");

			FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/dialog/confirmDialog.fxml"));
			Parent parent = loader.load();
			Scene scene = new Scene(parent);
			scene.getStylesheets().add(getClass().getResource("../../css/app.css").toString());

			Button confirmBtn = (Button) parent.lookup("#confirmBtn");
			Label confirmLbl = (Label) parent.lookup("#confirmLbl");
			confirmLbl.setText("로그아웃 하시겠습니까?");
			confirmBtn.setOnAction(event -> {
				thread.requestLogout();
				dialog.close();
			});

			ClientThread.posDialog(dialog);
			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// end 로그아웃 확인 다이알로그

	public void enterRoomDialog() { // 방 입장 시 비밀번호 입력 다이알로그
		try {
			Stage primaryStage = (Stage) fxRoomList.getScene().getWindow();
			Stage dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(primaryStage);
			dialog.setTitle("roomPwChk");

			FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/dialog/enterRoom.fxml"));
			Parent parent = loader.load();
			Scene scene = new Scene(parent);
			scene.getStylesheets().add(getClass().getResource("../../css/room.css").toString());

			Button closeBtn = (Button) parent.lookup("#closeBtn");
			Button enterRoomBtn = (Button) parent.lookup("#enterRoomBtn");
			pwChkLbl = (Label) parent.lookup("#pwChkLbl");
			TextField roomPwTf = (TextField) parent.lookup("#roomPwTf");

			closeBtn.setOnAction(event -> dialog.close());
			enterRoomBtn.setOnAction(event -> {
				password = roomPwTf.getText();
				if (password != null) {
					if (!password.equals("")) {
						thread.requestEnterRoom(roomNumber, password);
						password = "0";
					} else {
						password = "0";
						thread.requestEnterRoom(roomNumber, password);
					}
				} else {
					password = "0";
				}
				dialog.close();
			});

			ClientThread.posDialog(dialog);
			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// end 방 입장 시 비밀번호 입력 다이알로그

	public void resetComponents() { // 컴포넌트 초기화 작업
		roomNumber = 0;
		password = "0";
		doRock = false;
		fxWaitSend.setText("");
		fxWaitChatArea.setText("");
		fxWaitSend.requestFocus();
	}// end 초기화 작업

}
