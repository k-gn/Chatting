package application.db.vo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientVO {

	private String id;
	private String name;
	private String password;
	private String email;
	
	public ClientVO() {}

	public ClientVO(String id, String name, String password) {
		this.id = id;
		this.name = name;
		this.password = password;
	}
	
	public ClientVO(String id, String name, String password, String email) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.email = email;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean confirmPassword(String password) { // 비밀번호 확인
		if(this.password.equals(password)) {
			return true;
		}
		return false;
	}// end 비밀번호 확인
	
	public boolean isValidEmail(String email) { // 이메일 검증
		boolean check = false;
		String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(email);
		if (m.matches()) {
			check = true;
		}
		return check;
	}// end 이메일 검증
}
