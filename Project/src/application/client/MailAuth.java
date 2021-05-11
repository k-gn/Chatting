package application.client;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

// 메일서버를 통해 메일을 전송
public class MailAuth extends Authenticator { // SMTP(메일서버) 서버에 연결해 사용자 인증을 하기 위해서 Authenticator 클래스 사용

	private PasswordAuthentication pa;

	public MailAuth() {
		String mail_id = "rlarbska97@gmail.com";
		String mail_pw = "ssolove00@";
		pa = new PasswordAuthentication(mail_id, mail_pw);
	}

	public PasswordAuthentication getPasswordAuthentication() {
		return pa;
	}

}
