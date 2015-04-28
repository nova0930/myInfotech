package um.ece.abox.module.helper.db.rel.impl;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import um.ece.abox.module.helper.db.rel.DBConnection;

public class DBConnectionImpl implements DBConnection{

	String driver;
	String dbUri;
	String username;
	String password;
	java.sql.Connection conn;
	
	public DBConnectionImpl(String driver, String dbUri, 
							String username, String password) {
		this.driver = driver;
		this.dbUri = dbUri;
		this.username = username;
		this.password = password;
	}
	 
	public String getDriverInfo() {
		return this.driver;
	}

	
	public String getDatabaseURI() {
		return this.dbUri;
	}

	
	public void connect2Database() {
		try {
			Class.forName(driver);
			this.conn = DriverManager.getConnection(dbUri, username, password);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	public void closeConnection() {
		
		try {
			if (this.conn != null)
				this.conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}

	
	public ResultSet executeQuery(String table, Set<String> columns, String constraint) {
		
		// construct the SQL statement
		String sql = "SELECT";
		for (String col : columns)
			sql += " " + col + ",";
		sql = sql.substring(0, sql.length()-1);
		sql += " FROM " + table;
		sql += " " + constraint;
		// execute query
		Statement stmt = null;
		try {
			stmt = this.conn.createStatement();
			return stmt.executeQuery(sql.trim());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return null;
	}
}
