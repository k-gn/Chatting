package application.client.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import animatefx.animation.SlideInLeft;
import animatefx.animation.SlideInRight;
import animatefx.animation.ZoomIn;
import application.client.MailSend;
import application.db.dao.ClientDao;
import application.db.vo.ClientVO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginController implements Initializable {

	//---------------------------------------------------------------- 로그인 UI 컴포넌트
	@FXML private StackPane fxMainStack;
	@FXML private Pane fxLogPane;

	@FXML private Button fxLoginBtn;
	@FXML private Button fxFindBtn;
	@FXML private Button fxSignUpBtn;

	@FXML private TextField fxIdText;
	@FXML private PasswordField fxPwText;

	@FXML private CheckBox fxRememberId;

	@FXML private Label fxCheckLbl;

	//---------------------------------------------------------------- 회원가입 UI 컴포넌트
	@FXML private Pane fxSignPane;

	@FXML private TextField fxName;
	@FXML private TextField fxSignID;
	@FXML private TextField fxSignPW;
	@FXML private TextField fxSignPWCheck;
	@FXML private TextField fxSignEmail;

	@FXML private Button fxIdCheckBtn;
	@FXML private Button fxSignUpBtn2;
	@FXML private Button fxGoLogin1;

	@FXML private Label fxSignUpCheckLbl;

	//---------------------------------------------------------------- 아이디 비밀번호 찾기 UI 컴포넌트
	@FXML private Pane fxFindPane;
	@FXML private Button fxFindIdBtn;
	@FXML private Button fxFindPwBtn;
	@FXML private Button fxGoLogin2;

	//---------------------------------------------------------------- 클라이언트 대기실 UI 컴포넌트
	@FXML private Button fxEnterRoomBtn;
	@FXML private Button fxMakeRoomBtn;

	//---------------------------------------------------------------- 기타
	private Stage primaryStage;
	private boolean signChk = false;
	private ClientDao dao = ClientDao.getInstance();
	private String ip = "127.0.0.1";
	private int port = 9797;
	private String rememberId;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		clrscr();
	}

	public void setStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	public void clrscr() { // 화면 초기화
		if(rememberId != null) {
			fxIdText.setText(rememberId);
			fxRememberId.setSelected(true);
		}else {
			fxIdText.clear();
			fxRememberId.setSelected(false);
		}
		fxPwText.clear();
		fxCheckLbl.setText("");
		
		fxName.clear();
		fxSignID.clear();
		fxSignPW.clear();
		fxSignPWCheck.clear();
		fxSignEmail.clear();
		fxSignUpCheckLbl.setText("");
	}// end 화면 초기화

	public void handleButtonAction(ActionEvent event) { // 버튼 클릭 이벤트
		if(event.getSource().equals(fxSignUpBtn)) { // 회원가입 창 띄우기
			clrscr();
			new ZoomIn(fxSignPane).play();
			fxSignPane.toFront();
			return;
		} // end 회원가입 창 띄우기

		if(event.getSource().equals(fxFindBtn)) { // 아이디/비밀번호 찾기 창 띄우기
			new SlideInLeft(fxFindPane).play();
			fxFindPane.toFront();
			return;
		}// end 아이디/비밀번호 찾기 창 띄우기

		if(event.getSource().equals(fxGoLogin1)) { // 로그인창으로1
			new ZoomIn(fxLogPane).play();
			fxLogPane.toFront();
			return;
		}// end 로그인창으로1

		if(event.getSource().equals(fxGoLogin2)) { // 로그인창으로2
			new SlideInLeft(fxLogPane).play();
			fxLogPane.toFront();
			return;
		}// end 로그인창으로2

		if(event.getSource().equals(fxFindIdBtn)) { // 아이디 찾기 버튼 클릭
			setFindDialog("findIdForm");
			return;
		}// end 아이디 찾기 버튼 클릭

		if(event.getSource().equals(fxFindPwBtn)) { // 비밀번호 찾기 버튼 클릭
			setFindDialog("findPwForm");
			return;
		}// end 비밀번호 찾기 버튼 클릭

		if(event.getSource().equals(fxLoginBtn)) { // 로그인 버튼 클릭
			try {
				loginEvent();
			} catch (Exception e) {
				e.printStackTrace();
				fxCheckLbl.setTextFill(Color.RED);
				fxCheckLbl.setText("빈칸없이 입력해주세요.");
			}
			return;
		} // end 로그인 버튼 클릭
		
		if(event.getSource().equals(fxSignUpBtn2)) { // 회원가입 버튼 클릭
			try {
				signUpEvent();
			}catch (Exception e) {}
			return;
		} // end 회원가입 버튼 클릭
		
		if(event.getSource().equals(fxIdCheckBtn)) { // 아이디 중복확인 버튼 클릭
			signChk = dao.confirmId(fxSignID.getText());
			if(fxSignID.getText().equals("")) {
				fxSignUpCheckLbl.setTextFill(Color.RED);
				fxSignUpCheckLbl.setText("아이디를 입력해주세요.");
				return;
			}
			
			if(signChk) {
				fxSignUpCheckLbl.setTextFill(Color.BLUE);
				fxSignUpCheckLbl.setText("사용가능한 아이디 입니다.");
			}else {
				fxSignUpCheckLbl.setTextFill(Color.RED);
				fxSignUpCheckLbl.setText("중복된 아이디 입니다.");
			}
			return;
		} // end 아이디 중복확인 버튼 클릭
	}// end 버튼 클릭 이벤트
	
	public void handleKeyAction(KeyEvent key) { // 키 이벤트
		if(key.getCode() == KeyCode.ENTER && key.getSource().equals(fxPwText)) { // 로그인 엔터키 이벤트
			try {
				loginEvent();
			} catch (Exception e) {
				e.printStackTrace();
				fxCheckLbl.setText("빈칸없이 입력해주세요.");
			}
			return;
		}
		
		if(key.getCode() == KeyCode.ENTER && key.getSource().equals(fxSignEmail)) { // 회원가입 엔터키 이벤트
			try {
				signUpEvent();
			}catch (Exception e) {}
			return;
		}
	}// end 키 이벤트

	private void setSignConfirmDialog(boolean flag) { // 확인 다이알로그
		Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("Confirm");
		Parent parent = null;
		Scene scene = null;
		try {
			parent = FXMLLoader.load(getClass().getResource("../fxml/dialog/confirmDialog.fxml"));
			scene = new Scene(parent);
			scene.getStylesheets().add(getClass().getResource("../../css/app.css").toString());
			Button confirmBtn = (Button) parent.lookup("#confirmBtn");
			Label confirmLbl = (Label) parent.lookup("#confirmLbl");
			
			if(flag) {
				confirmLbl.setText("회원가입 성공!");
			}else {
				confirmLbl.setText("회원가입 실패!");
			}
			confirmBtn.setOnAction(event -> { // 다이알로그 로그인 홈 버튼 이벤트
				dialog.close();
				if(flag) {
					new ZoomIn(fxLogPane).play();
					fxLogPane.toFront();
				}
			});
		} catch (IOException e) {}
		ClientThread.posDialog(dialog);
		dialog.setScene(scene);
		dialog.setResizable(false);
		dialog.show();
	}// end 확인 다이알로그

	public void setFindDialog(String formName) { // 아이디/비밀번호 찾기 다이알로그
		Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("FindIdPw");
		Parent parent = null;
		Scene scene = null;
		try {
			parent = FXMLLoader.load(getClass().getResource("../fxml/dialog/"+ formName +".fxml"));
			scene = new Scene(parent);
			scene.getStylesheets().add(getClass().getResource("../../css/app.css").toString());
			Button goLogin = (Button) parent.lookup("#goLogin");
			goLogin.setOnAction(event -> { // 다이알로그 로그인 홈 버튼 이벤트
				dialog.close();
				new SlideInLeft(fxLogPane).play();
				fxLogPane.toFront();
			});

			if(formName.equals("findIdForm")) { // 정보 입력 후 아이디 찾기 버튼 클릭 
				Pane idFindResultPane = (Pane) parent.lookup("#idFindResultPane");
				Button idFindConfirm = (Button) parent.lookup("#idFindConfirm");
				Button idConfirmBtn = (Button) parent.lookup("#idConfirmBtn");
				TextField name = (TextField) parent.lookup("#idFindNameField");
				TextField email = (TextField) parent.lookup("#idFindEmailField");
				Label checkLbl = (Label) parent.lookup("#checkLbl");
				Label resultLbl = (Label) parent.lookup("#resultLbl");
				
				email.setOnKeyPressed(key -> {
					if(key.getCode() == KeyCode.ENTER) {
						String id = dao.findId(name.getText(), email.getText());
						if(id.equals("")) {
							checkLbl.setText("해당하는 아이디가 없습니다.");
							return;
						}
						resultLbl.setText("아이디는 " + id + "입니다.");
						new SlideInRight(idFindResultPane).play();
						idFindResultPane.toFront();
					}
				});
				idFindConfirm.setOnAction(event -> { // 아이디 찾기창 확인버튼 이벤트
					String id = dao.findId(name.getText(), email.getText());
					if(id.equals("")) {
						checkLbl.setText("해당하는 아이디가 없습니다.");
						return;
					}
					resultLbl.setText("아이디는 " + id + "입니다.");
					new SlideInRight(idFindResultPane).play();
					idFindResultPane.toFront();
				});
				idConfirmBtn.setOnAction(e -> { // 아이디 확인창 확인버튼 이벤트
					dialog.close();
					new SlideInLeft(fxLogPane).play();
					fxLogPane.toFront();
				});

			}else if(formName.equals("findPwForm")) { // 정보 입력 후 비밀번호 찾기 버튼 클릭
				Pane pwResetPane = (Pane) parent.lookup("#pwResetPane");
				Pane pwResetResultPane = (Pane) parent.lookup("#pwResetResultPane");
				Button findPwConfirm = (Button) parent.lookup("#findPwConfirm");
				Button pwResetConfirmBtn = (Button) parent.lookup("#pwResetConfirmBtn");
				Button pwConfirmBtn = (Button) parent.lookup("#pwConfirmBtn");
				TextField id = (TextField) parent.lookup("#pwFindIdField");
				TextField email = (TextField) parent.lookup("#pwFindEmailField");
				PasswordField resetPw = (PasswordField) parent.lookup("#resetPw");
				PasswordField resetPwCheck = (PasswordField) parent.lookup("#resetPwCheck");
				Label checkLbl1 = (Label) parent.lookup("#checkLbl1");
				Label checkLbl2 = (Label) parent.lookup("#checkLbl2");
				
				email.setOnKeyPressed(key -> {
					if(key.getCode() == KeyCode.ENTER) {
						String pw = dao.findPw(id.getText(), email.getText());
						if(!pw.equals("")) {
							int code = MailSend.sendMail(email.getText(), pw);
							if(code == 0) {
								checkLbl1.setTextFill(Color.BLUE);
								checkLbl1.setText("비밀번호 전송!(이메일을 확인해주세요.)");
							}else {
								checkLbl1.setTextFill(Color.RED);
								checkLbl1.setText("메일 전송이 실패했습니다.");
							}
						}else {
							checkLbl1.setTextFill(Color.RED);
							checkLbl1.setText("해당하는 아이디가 없습니다.");
						}
					}
				});
				findPwConfirm.setOnAction(event -> { // 비밀번호 찾기창 확인버튼 이벤트
					String pw = dao.findPw(id.getText(), email.getText());
					if(!pw.equals("")) {
						int code = MailSend.sendMail(email.getText(), pw);
						if(code == 0) {
							checkLbl1.setTextFill(Color.BLUE);
							checkLbl1.setText("비밀번호 전송!(이메일을 확인해주세요.)");
						}else {
							checkLbl1.setTextFill(Color.RED);
							checkLbl1.setText("메일 전송이 실패했습니다.");
						}
					}else {
						checkLbl1.setTextFill(Color.RED);
						checkLbl1.setText("해당하는 아이디가 없습니다.");
					}
				});
			}
		} catch (IOException e) {
		}
		ClientThread.posDialog(dialog);
		dialog.setScene(scene);
		dialog.setResizable(false);
		dialog.show();
	}// end 아이디/비밀번호 찾기 다이알로그

	public void signUpEvent() throws Exception { // 회원가입 이벤트
		String id = fxSignID.getText();
		String name = fxName.getText();
		String password = fxSignPW.getText();
		String confirmPassword = fxSignPWCheck.getText();
		String email = fxSignEmail.getText();
		if(id.equals("") || name.equals("") || password.equals("") || email.equals("")) {
			fxSignUpCheckLbl.setTextFill(Color.RED);
			fxSignUpCheckLbl.setText("빈칸없이 입력해주세요.");
			return;
		}
		ClientVO client = new ClientVO(id, name, password);
		if(!client.confirmPassword(confirmPassword)) {
			fxSignUpCheckLbl.setTextFill(Color.RED);
			fxSignUpCheckLbl.setText("비밀번호 확인란이 틀렸습니다.");
			return;
		}
		
		if(!client.isValidEmail(email)) {
			fxSignUpCheckLbl.setTextFill(Color.RED);
			fxSignUpCheckLbl.setText("이메일 형식이 올바르지 않습니다.");
			return;
		}else {
			client.setEmail(email);
		}
		
		if(client.confirmPassword(confirmPassword) && signChk) {
			signChk = false;
			dao.insertUser(client);
			setSignConfirmDialog(true);
		}else {
			fxSignUpCheckLbl.setTextFill(Color.RED);
			fxSignUpCheckLbl.setText("아이디 중복체크를 해주세요.");
			setSignConfirmDialog(false);
		}
	}// end 회원가입 이벤트
	
	public void loginEvent() throws Exception { // 로그인 이벤트
		String id = fxIdText.getText();
		String password = fxPwText.getText();
		
		int resultNum = dao.userCheck(id, password);
		if(resultNum == ClientDao.LOGIN_SUCCESS) {
			startClient(id, ip, port);
		}else if(resultNum == ClientDao.LOGIN_FAIL_PW) {
			fxCheckLbl.setText("비밀번호가 일치하지 않습니다.");
		}else if(resultNum == ClientDao.LOGIN_FAIL_ID) {
			fxCheckLbl.setText("아이디가 일치하지 않습니다.");
		}
	}// end 로그인 이벤트
	
	public void startClient(String id, String ip, int port) { // 클라이언트 시작
		try {
			ClientThread thread = new ClientThread();
			thread.setPrimaryStage(primaryStage);
			if(fxRememberId.isSelected()) {
				thread.setRemember(true);
			}else {
				thread.setRemember(false);
				rememberId = null;
			}
			thread.setController(this);
			thread.start(ip, port);
			thread.requestLogin(id);
		}catch (Exception e) {
		}
	}// end 클라이언트 시작

	public Label getFxCheckLbl() {
		return fxCheckLbl;
	}
	
	public void setId(String rememberId) {
		this.rememberId = rememberId;
	}
} // end main
