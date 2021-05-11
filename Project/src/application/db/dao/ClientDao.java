package application.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import application.db.util.ConnUtil;
import application.db.util.CloseUtil;
import application.db.vo.ClientVO;

public class ClientDao {

	private static ClientDao dao = new ClientDao();
	private static Properties pro = ConnUtil.getProperties();
	public static final int LOGIN_SUCCESS = 1;
	public static final int LOGIN_FAIL_ID = -1;
	public static final int LOGIN_FAIL_PW = 0;

	public static ClientDao getInstance() {
		if (dao == null) {
			synchronized(ClientDao.class) {
				if (dao == null) {
					dao = new ClientDao();
				}
			}
		}
		return dao;
	}
	
	// 가입 시 ID중복확인을 하는 메서드 선언.
	public boolean confirmId(String id) {
		boolean flag = false;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ConnUtil.getConenction();
			pstmt = conn.prepareStatement(pro.getProperty("confirm_id"));
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				flag = false;
			} else {
				flag = true;
			}
		} catch (Exception e) {
		} finally {
			CloseUtil.close(conn);
			CloseUtil.close(pstmt);
			CloseUtil.close(rs);
		}
		return flag;
	}
	
	// 이름과 이메일로 아이디를 찾는 메서드 선언.
	public String findId(String name, String email) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String id = "";
		try {
			conn = ConnUtil.getConenction();
			pstmt = conn.prepareStatement(pro.getProperty("find_id"));
			pstmt.setString(1, name);
			pstmt.setString(2, email);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				id = rs.getString("user_id");
			}
		} catch (Exception e) {
		} finally {
			CloseUtil.close(conn);
			CloseUtil.close(pstmt);
			CloseUtil.close(rs);
		}
		return id;
	}
	
	// id와 이메일로 비밀번호를 찾는 메서드 선언.
	public String findPw(String id, String email) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String pw = "";
		try {
			conn = ConnUtil.getConenction();
			pstmt = conn.prepareStatement(pro.getProperty("find_pw"));
			pstmt.setString(1, id);
			pstmt.setString(2, email);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				pw = rs.getString("user_pw");
			}
		} catch (Exception e) {
		} finally {
			CloseUtil.close(conn);
			CloseUtil.close(pstmt);
			CloseUtil.close(rs);
		}
		return pw;
	}

	// 회원가입을 수행하는 메서드 선언.
	public boolean insertUser(ClientVO client) {
		boolean flag = false;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = ConnUtil.getConenction();
			pstmt = conn.prepareStatement(pro.getProperty("insert"));
			pstmt.setString(1, client.getId());
			pstmt.setString(2, client.getName());
			pstmt.setString(3, client.getPassword());
			pstmt.setString(4, client.getEmail());
			int i = pstmt.executeUpdate();
			if (i == 1) {
				flag = true;
			} else {
				flag = false;
			}
		} catch (Exception e) {
		} finally {
			CloseUtil.close(conn);
			CloseUtil.close(pstmt);
		}
		return flag;
	}

	// 로그인의 유효성을 검증하는 메서드 선언.
	public int userCheck(String id, String pw) {
		int resultNum = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ConnUtil.getConenction();
			pstmt = conn.prepareStatement(pro.getProperty("user_check"));
			pstmt.setString(1, id);

			rs = pstmt.executeQuery();
			if (rs.next()) {
				String dbPw = rs.getString("user_pw");
				if (dbPw.equals(pw)) { // 로그인에 성공한 경우
					resultNum = LOGIN_SUCCESS;
				} else { // 비밀번호가 틀린 경우
					resultNum = LOGIN_FAIL_PW;
				}
			} else { // 아이디 존재하지 않는 경우
				resultNum = LOGIN_FAIL_ID;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseUtil.close(conn);
			CloseUtil.close(pstmt);
			CloseUtil.close(rs);
		}
		return resultNum;
	}

	// 로그인한 회원의 회원 정보를 모두 가져오는 메서드 선언
	public ClientVO getUserInfo(String id) {
		ClientVO client = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ConnUtil.getConenction();
			pstmt = conn.prepareStatement(pro.getProperty("user_info"));
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				client = new ClientVO(rs.getString("user_id"), 
						rs.getString("user_name"), rs.getString("user_pw"), 
						rs.getString("user_email"));
			}
		} catch (SQLException e) {
		} finally {
			CloseUtil.close(conn);
			CloseUtil.close(pstmt);
			CloseUtil.close(rs);
		}
		return client;
	}

	// 비밀번호 변경
	public boolean changePassword(String id, String pw) {
		boolean flag = false;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = ConnUtil.getConenction();
			pstmt = conn.prepareStatement(pro.getProperty("change_pw"));
			pstmt.setString(1, pw);
			pstmt.setString(2, id);
			int i = pstmt.executeUpdate();
			if (i == 1) {
				flag = true;
			} else {
				flag = false;
			}
		} catch (SQLException e) {
		} finally {
			CloseUtil.close(conn);
			CloseUtil.close(pstmt);
		}
		return flag;
	}

	// 회원 정보 수정
	public boolean updateUser(ClientVO client) {
		boolean flag = false;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = ConnUtil.getConenction();
			pstmt = conn.prepareStatement(pro.getProperty("update"));
			pstmt.setString(1, client.getEmail());
			pstmt.setString(2, client.getId());
			int i = pstmt.executeUpdate();
			if (i == 1) {
				flag = true;
			} else {
				flag = false;
			}
		} catch (Exception e) {
		} finally {
			CloseUtil.close(conn);
			CloseUtil.close(pstmt);
		}
		return flag;
	}

}
