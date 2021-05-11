package application.db.util;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnUtil {
	private static Properties pro = new Properties();
	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			pro.load(new FileInputStream("src/application/db/database.properties"));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Connection getConenction() throws SQLException {
		return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/XEPDB1", "mytest", "mytest");
	}
	
	public static Properties getProperties() {
		return pro;
	}
}
