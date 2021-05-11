package application.client.file;

import java.net.ServerSocket;
import java.net.Socket;

import application.client.controller.ClientThread;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ReceiveFile {

	private ServerSocket serverSocket;
	private Socket socket;
	private FileThread client;
	
	Stage dialog;
	Button closeBtn;
	Label checkLbl;
	TextArea receiveArea;
	
	private static final String SEPARATOR = "-";
	
	public ReceiveFile(Stage primaryStage) {
		try {
			dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(primaryStage);
			dialog.setTitle("fileReceive");

			FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/dialog/fileReceive.fxml"));
			Parent parent = loader.load();
			
			closeBtn = (Button) parent.lookup("#closeBtn");
			closeBtn.setVisible(false);
			checkLbl = (Label) parent.lookup("#checkLbl");
			receiveArea = (TextArea) parent.lookup("#receiveArea");
			receiveArea.setEditable(false);
			
			Scene scene = new Scene(parent);
			scene.getStylesheets().add(getClass().getResource("../../css/app.css").toString());
			
			closeBtn.setOnAction(event -> { // 종료 버튼 이벤트
				dialog.close();
			});
			
			ClientThread.posDialog(dialog);
			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.show();
			
			startFileThread();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startFileThread() { // 파일스레드 시작
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					serverSocket = new ServerSocket(9898);
				}catch (Exception e) {
					e.printStackTrace();
				}
				try {
					socket = serverSocket.accept();
					client = new FileThread(ReceiveFile.this, socket, serverSocket);
					client.start();
				}catch (Exception e) {
					e.printStackTrace();
					try {if(socket != null) socket.close();}catch (Exception e1) {}
					try {if(serverSocket != null) serverSocket.close();}catch (Exception e1) {}
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}// end 파일스레드 시작
}
