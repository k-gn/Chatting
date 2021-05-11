package application.client.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import javafx.application.Platform;
import javafx.scene.paint.Color;

public class FileThread extends Thread {
	
	private ReceiveFile recv;
	private Socket socket;
	private ServerSocket serverSocket;
	
	private FileOutputStream fos;
	private BufferedOutputStream bos;
	private BufferedInputStream bis;
	private DataInputStream dis;
	
	private String header;
	private byte[] data;
	private File file;
	
	private static final String SEPARATOR = "-";

	// 생성자
	public FileThread(ReceiveFile receiveFile, Socket socket, ServerSocket serverSocket) {
		try {
			recv = receiveFile;
			this.serverSocket = serverSocket;
			this.socket = socket;
			dis = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
		}
	}
	
	@Override
	public void run() { // 스레드 동작
		try {
			header = dis.readUTF();
			StringTokenizer st = new StringTokenizer(header, SEPARATOR);
			String filename = st.nextToken();
			int fileLength = Integer.parseInt(st.nextToken());
			data = new byte[fileLength];
			bis = new BufferedInputStream(dis);
			
			File dir = new File("C:\\ChatProgram\\");
			if(!dir.exists()) {
				dir.mkdir();
			}
			
			file = new File(dir, filename);
			if(file.exists()) {
				file = new File(dir, "re_" + filename);
				Platform.runLater(() -> {
					recv.checkLbl.setTextFill(Color.DODGERBLUE);
					recv.checkLbl.setText("파일이 이미 존재합니다.");
					recv.closeBtn.setVisible(true);
				});
			}else if(!file.createNewFile()){
				Platform.runLater(() -> {
					recv.checkLbl.setTextFill(Color.RED);
					recv.checkLbl.setText("파일 생성 에러...(수신 취소)");
					recv.closeBtn.setVisible(true);
				});
				return;
			}else {
				Platform.runLater(() -> {
					recv.checkLbl.setTextFill(Color.DODGERBLUE);
					recv.checkLbl.setText("파일 수신이 성공했습니다.");
					recv.receiveArea.appendText(filename + " 저장위치 : " + dir.getAbsolutePath() + "\\" + file.getName() + "\n");
					recv.closeBtn.setVisible(true);
				});
			}
			
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos, fileLength);
			
			int readNum = 0;
			while((readNum = bis.read(data)) != -1) {
				bos.write(data, 0, fileLength);
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {if(bis != null) bis.close();} catch (IOException e) {}
			try {if(dis != null) dis.close();} catch (IOException e) {}
			try {if(bos != null) bos.close();} catch (IOException e) {}
			try {if(fos != null) fos.close();} catch (IOException e) {}
			try {if(socket != null) socket.close();} catch (IOException e) {}
			try {if(serverSocket != null) serverSocket.close();} catch (IOException e) {}
		}
	}// end run

}
