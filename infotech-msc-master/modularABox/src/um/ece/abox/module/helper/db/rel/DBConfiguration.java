package um.ece.abox.module.helper.db.rel;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/**
 * Parse XML files to get information of mapped databases, tables and columns.
 * The xml file is organized using OWL, the web ontology language. 
 * @author Jia
 *
 */
public interface DBConfiguration {
	
	
	
	/**
	 * Read the OWL ontology model of the database tables.
	 * Get the table names as OWL Class.
	 * Get the mapping relationships as OWL Object Properties.
	 * 
	 * @param url is for the configuration file.
	 */
	public void loadDatabaseModel(String url);
	
	
	/**
	 * Read the OWL ontology mapping of the database and its tables.
	 * Get the database URI, JDBC driver, userName, and password.
	 * @param url
	 */
	public void loadDatabaseMapping(String url);
	
	
	/**
	 * Get the Domain (DB table name) of the OWL object property 
	 * used for mapping database tables.
	 * @return
	 */
	public String getDomainTableName(String column);
	
	
	/**
	 * Get the Range (DB table name) of the OWL object property 
	 * used for mapping database tables.
	 * @param role
	 * @return
	 */
	public String getRangeTableName(String column);
	
	
	/**
	 * Given a DB table name, get the database IRI.
	 * @param columnName
	 * @return
	 */
	public String getDatabaseIRI(String table);
	
	
	/**
	 * Given a DB table name, get all its columns that are used
	 * for database mapping (i.e. used as OWL object properties)
	 * @param tableName
	 * @return
	 */
	public Set<String> getColumns(String table);
	
	
	/**
	 * Given an IRI of database instance (i.e. OWL instance), get all
	 * its tables.
	 * @param dbIRI
	 * @return
	 */
	public Set<String> getTables(String dbIRI);
	
	
	/**
	 * Given a IRI of database instance, get its delegate for 
	 * connection and query execution.
	 * @param dbIRI
	 * @return
	 */
	public DBConnection getDatabaseConnection(String dbIRI);
	
	/**
	 * Get all database connection delegate objects.
	 * @return
	 */
	public Set<DBConnection> getDatabaseConnections();
	
	
	/**
	 * Given the column name of a table, get the corresponding 
	 * OWLObjectPropertyExpression.
	 * @param column
	 * @return
	 */
	public OWLObjectPropertyExpression getOWLObjectProperty(String column);
}
