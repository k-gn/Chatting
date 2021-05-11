package application.client;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSend {

	private static Properties prop = new Properties();
	private static Authenticator auth = new MailAuth();
	
	public static int sendMail(String email, String pw) {
		// TLS 설정
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", "smtp.gmail.com");
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.port", "587");
		
		// 메일 전송하기
		try {
			Session session = Session.getDefaultInstance(prop, auth);
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("rlarbska97@gmail.com", "Admin"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			msg.setSubject("<Chatting Program> Find your Password");
			msg.setText("# Your Password : " + pw);
			Transport.send(msg);
		} catch (AddressException e) {
			return -1;
		} catch (MessagingException e) {
			return -1;
		} catch (UnsupportedEncodingException e) {
			return -1;
		}
		return 0;
	}
}
