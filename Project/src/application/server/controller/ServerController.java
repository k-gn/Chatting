package application.server.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ServerController implements Initializable {

	//---------------------------------------------------------------------- 서버 버튼
	@FXML private Button serverBtn;
	@FXML private Button saveBtn;
	@FXML private TextArea logArea;
	@FXML private ListView<String> connectArea;

	//---------------------------------------------------------------------- 기타
	public static ExecutorService executorService; 
	public static Vector<ServerThread> clients = new Vector<>();
	public static Vector<String> ids = new Vector<>();
	public static final int maxClient = 50;
	public static String ip = "127.0.0.1";
	public static int port = 9797;
	private ServerSocket serverSocket;
	private Socket socket;
	private Stage primaryStage;
	
	private BufferedWriter bw;
	private PrintWriter writer;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logArea.setEditable(false);
	}
	
	public void setPrimaryStage(Stage primaryStage) { // Stage Setter 및 close 이벤트 
		this.primaryStage = primaryStage;
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				stopServer();
			}
		});
	}

	public void handleButtonAction(ActionEvent event) { // 서버 버튼 이벤트 메소드
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("YY/MM/DD a HH:mm:ss");
		if(event.getSource().equals(serverBtn)) { // 서버 시작 버튼
			if(serverBtn.getText().equals("시작하기")) {
				serverBtn.setText("종료하기");
				String message = String.format("[" + sdf.format(date) + "] - " + "[서버 시작 - %s : %d]\n", ip, port);
				logArea.appendText(message);
				startServer(ip, port);
			}else if(serverBtn.getText().equals("종료하기")){
				serverBtn.setText("시작하기");
				String message = String.format("[" + sdf.format(date) + "] - " + "[서버 종료 - %s : %d]\n", ip, port);
				logArea.appendText(message);
				connectArea.getItems().clear();
				stopServer();
			}
		}
		
		if(event.getSource().equals(saveBtn)) { // 저장 버튼
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = 
                    new FileChooser.ExtensionFilter("TEXT files (*.txt)", "*.txt");
			fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle("Save");
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                saveTextToFile(logArea.getText(), file);
            }
		}
	}// end 서버 버튼 이벤트 메소드

	private void saveTextToFile(String text, File file) { // 텍스트 파일 저장하기
		try {
			bw = new BufferedWriter(new FileWriter(file));
			writer = new PrintWriter(bw, true);
            writer.println(text);
            writer.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}// end 텍스트 파일 저장

	void displayText(String text) { // 로그창에 글 띄우기
		logArea.appendText(text + "\n");
	}
	
	//Getter
	public TextArea getLogArea() {
		return logArea;
	}
	
	public ListView<String> getConnectArea() {
		return connectArea;
	}

	public void startServer(String ip, int port) { // 서버 시작 메소드
		try {
			executorService = Executors.newFixedThreadPool( // 스레드풀 생성
					Runtime.getRuntime().availableProcessors() // cpu 코어 수만큼 생성
				);
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(ip, port));
		} catch (Exception e) {
			if(!serverSocket.isClosed()) stopServer(); return;
		}
		
		Runnable runnable = new Runnable() { // 작업 생성
			@Override
			public void run() {
				while(true) {
					ServerThread client = null;
					try {
						socket = serverSocket.accept();
						client = new ServerThread(socket);
						client.setController(ServerController.this);
						executorService.submit(client);
					} catch (IOException e) {
						if(!serverSocket.isClosed()) stopServer();
						break;
					}
				}
			}
		};
		executorService.submit(runnable);
	}// end 서버 시작 메소드
	
	public void stopServer() { // 서버의 작동을 중지 메소드 (전체 자원 해제)
		try {
			Iterator<ServerThread> iterator = clients.iterator();
			while(iterator.hasNext()) {
				iterator.next().socket.close();
			}
			clients.clear();
			ids.clear();
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			if(executorService != null && !executorService.isShutdown()) {
				executorService.shutdown();
			}
		} catch (Exception e) {}
	}// end 서버 중지 메소드
}
