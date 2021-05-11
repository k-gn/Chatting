package application.client.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import application.client.controller.ClientThread;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SendFile {
	
	private TextField filePath;
	private Button fileFindBtn;
	private Button fileSendBtn;
	private Button closeBtn;
	private Label checkLbl;
	
	private Stage primaryStage;
	private String address;
	private static final String SEPARATOR = "-";
	
	private Socket socket;
	private BufferedOutputStream bos;
	private FileInputStream fis;
	private BufferedInputStream bis;
	private DataOutputStream out;

	int count;
	int readNum;
	
	// 생성자
	public SendFile(String addr, Stage primaryStage) { 
		address = addr;
		try {
			Stage dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(primaryStage);
			dialog.setTitle("fileTrans");

			FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/dialog/fileTrans.fxml"));
			Parent parent = loader.load();
			Scene scene = new Scene(parent);
			filePath = (TextField) parent.lookup("#filePath");
			fileFindBtn = (Button) parent.lookup("#fileFindBtn");
			fileSendBtn = (Button) parent.lookup("#fileSendBtn");
			checkLbl = (Label) parent.lookup("#checkLbl");
			closeBtn = (Button) parent.lookup("#closeBtn");
			
			scene.getStylesheets().add(getClass().getResource("../../css/app.css").toString());
			
			fileFindBtn.setOnAction(event -> { // 찾아보기 버튼 이벤트
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open");
				ExtensionFilter allType = new ExtensionFilter("All Files", "*.*");
				ExtensionFilter imgType = new ExtensionFilter("Image file", "*.jpg", "*.gif", "*.png");
				ExtensionFilter txtType = new ExtensionFilter("Text file", "*.txt", "*.doc");
				fileChooser.getExtensionFilters().addAll(allType, imgType, txtType);
	            File file = fileChooser.showOpenDialog(primaryStage);
	            if(file != null) {
	            	filePath.setText(file.getAbsolutePath());
	            }
			});
			
			fileSendBtn.setOnAction(event -> { // 전송 버튼 이벤트
				if(filePath.getText().equals("")) {
					checkLbl.setTextFill(Color.RED);
					checkLbl.setText("보낼 파일을 선택해주세요.");
					return;
				}
				
				File file = new File(filePath.getText());
				if(!file.exists()) {
					checkLbl.setTextFill(Color.RED);
					checkLbl.setText("해당파일을 찾을 수 없습니다.");
					return;
				}
				
				StringBuffer sb = new StringBuffer();
				int fileLength = (int)file.length();
				
				sb.append(file.getName());
				sb.append(SEPARATOR);
				sb.append(fileLength);
				
				try {
					socket = new Socket(address, 9898);
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis, fileLength);
					byte data[] = new byte[fileLength];
					
					out = new DataOutputStream(socket.getOutputStream());
					out.writeUTF(sb.toString());
					filePath.setText("");
					
					bos = new BufferedOutputStream(out);
					count = 0;
					
					try {
						readNum = 0;
						while((readNum = bis.read(data)) != -1) {
							bos.write(data, 0, fileLength);
						}
						bos.flush();
					}catch (IOException e) {
						checkLbl.setTextFill(Color.RED);
						checkLbl.setText("파일읽기 오류...");
						return;
					}
					
					checkLbl.setTextFill(Color.DODGERBLUE);
					checkLbl.setText("파일 전송이 완료되었습니다.");
				}catch (Exception e) {
					checkLbl.setTextFill(Color.RED);
					checkLbl.setText("연결에 실패하였습니다.");
				}finally {
					try { if(bis != null) bis.close();} catch (IOException e) {}
					try { if(fis != null) fis.close();} catch (IOException e) {}
					try { if(out != null) out.close();} catch (IOException e) {}
					try { if(bos != null) bos.close();} catch (IOException e) {}
					try { if(socket != null) socket.close();} catch (IOException e) {}
				}
			});
			
			closeBtn.setOnAction(event -> { // 종료 버튼 이벤트
				dialog.close();
			});

			ClientThread.posDialog(dialog);
			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
