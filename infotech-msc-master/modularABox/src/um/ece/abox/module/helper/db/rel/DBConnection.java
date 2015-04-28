package um.ece.abox.module.helper.db.rel;

import java.sql.ResultSet;
import java.util.Set;

/**
 * 1. Establish and maintain connection to databases.
 * 2. Maintain information of the database, tables, and columns that
 * 	  are indicated to be object property.
 * 3. Generate SQL query
 * 4. Execute SQL query
 * 
 * @author Jia
 *
 */
public interface DBConnection {

	/**
	 * Get the driver information for the databse.
	 * @return
	 */
	public String getDriverInfo();
	
	/**
	 * Get the URI of the database.
	 * @return
	 */
	public String getDatabaseURI();
	
	/**
	 * Establish connection to the database.
	 */
	public void connect2Database();
	
	/**
	 * Close the connection to the databse.
	 */
	public void closeConnection();
	
	
	/**
	 * Execute the SQL query, based on the given parameters.
	 * @param table
	 * @param columns
	 * @param constraint
	 * @return
	 */
	public ResultSet executeQuery(String table, Set<String> columns, String constraint);
	
}
