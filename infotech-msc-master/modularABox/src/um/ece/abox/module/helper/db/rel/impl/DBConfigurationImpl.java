package um.ece.abox.module.helper.db.rel.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import um.ece.abox.module.helper.db.rel.DBConfiguration;
import um.ece.abox.module.helper.db.rel.DBConnection;

public class DBConfigurationImpl implements DBConfiguration{

	public static char URI_CLASS_DELIM = '#';
	public static char TABLE_COLUMN_DELIM = '.';
	
	// Map<DB OWL individual IRI, its tables>
	private Map<String, Set<String>> db_table;
	
	// Map<Table, DB IRI>
	private Map<String, String> table_db;
		
	// Map<table name, names of table fields used for mapping>
	private Map<String, Set<String>> table_column;
			
	// Map<table column name, the same column as OWL object properties for mapping>
	private Map<String, OWLObjectProperty> column_role; 
		
	// Map<DB OWL individual IRI, its connection to underlying DB>
	private Map<String, DBConnection> db_conn;	
		
	// Configuration file as an OWL ontology.
	protected OWLOntology config;
		
	public DBConfigurationImpl() {
		this.db_table = new HashMap<String, Set<String>>();
		this.table_db = new HashMap<String, String>();
		this.table_column = new HashMap<String, Set<String>>();
		this.column_role = new HashMap<String, OWLObjectProperty>();
		this.db_conn = new HashMap<String, DBConnection>();
	}	
	
	public void loadDatabaseModel(String url) {
		// load configuration file as an ontology.
		File f = new File(url);
		try {
				this.config = OWLManager.createOWLOntologyManager()
											.loadOntologyFromOntologyDocument(f);
				
				// get all database tables as OWL Classes
				Set<OWLClass> classes = this.config.getClassesInSignature();
				// get all mapping relationships as OWL object properties
				Set<OWLObjectProperty> roles = this.config.getObjectPropertiesInSignature();
				
				for (OWLClass c : classes) {
					String s = c.toString();
					// get the name of the corresponding database table
					s = s.substring(s.lastIndexOf(URI_CLASS_DELIM)+1, s.length()-1);
					this.table_column.put(s, new HashSet<String>());
				}
				
				for (OWLObjectProperty r : roles)  {
					String domain = this.getObjectPropertyDomain(r);
					String s = r.toString();
					s = s.substring(s.lastIndexOf(TABLE_COLUMN_DELIM)+1, s.length()-1);
					this.table_column.get(domain).add(s);
					this.column_role.put(s, r);
				}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void loadDatabaseMapping(String url) {
		
		// load configuration file as an ontology.
		File f = new File(url);
		try {
			 OWLOntology dbmap = OWLManager.createOWLOntologyManager()
									.loadOntologyFromOntologyDocument(f);
			// get all mapping declarations as ABox assertions
			Set<OWLAxiom> assertions = dbmap.getABoxAxioms(false);
			
			Set<OWLNamedIndividual> tables = new HashSet<OWLNamedIndividual>();
			Set<OWLNamedIndividual> databases = new HashSet<OWLNamedIndividual>();
			
			// get individuals representing tables, and database, respectively
			for (OWLAxiom ax : assertions) {
				OWLClassAssertionAxiom axx = (OWLClassAssertionAxiom) ax;
				OWLClassExpression c = axx.getClassExpression();
				OWLNamedIndividual ind = axx.getIndividual().asOWLNamedIndividual();
				
				if (c.toString().contains("ClassMap"))
					tables.add(ind);
				else if (c.toString().contains("Database"))
					databases.add(ind);
			}
			
			// gather database information & create database connection
			for (OWLNamedIndividual db : databases) {
				// create Map<DBInd_IRI, TableInd_IRI's>
				this.db_table.put(db.toString(), new HashSet<String>());
				
				// create Map<DBInd_IRI, DBConnection's>
				String driver=null, dburi=null, username=null, password="";
				Set<OWLAnnotationAssertionAxiom> info = dbmap.getAnnotationAssertionAxioms(db.getIRI());
				for (OWLAnnotationAssertionAxiom ann : info) {
					String property = ann.getProperty().toString();
					String value = ann.getValue().toString();
					if (property.contains("jdbcDriver"))
						driver = value;
					else if (property.contains("jdbcDSN"))
						dburi = value;
					else if (property.contains("username"))
						username = value;
					else if (property.contains("password"))
						password = value;
				}
				
				DBConnection conn = new DBConnectionImpl(driver, dburi,username, password);
				this.db_conn.put(db.toString(), conn);
			}
			
			// mapping tables to database
			for (OWLNamedIndividual t : tables) {
				// create Map<TableInd_IRI, ColumnInd_IRI>
				String tableName = t.toString();
				tableName = tableName.substring(tableName.lastIndexOf(URI_CLASS_DELIM)+1, tableName.length()-1);
				// get information of the table
				Set<OWLAnnotationAssertionAxiom> info = dbmap.getAnnotationAssertionAxioms(t.getIRI());	
				for (OWLAnnotationAssertionAxiom ann : info) {
					if (ann.getProperty().toString().contains("dataStorage")) {
						String db = ann.getValue().toString();
						this.db_table.get(db).add(tableName);
						this.table_db.put(tableName, db);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public String getObjectPropertyDomain(OWLObjectPropertyExpression role) {
		
		OWLClassExpression domain = role.getDomains(this.config).iterator().next();
		String s = domain.toString();
		return s.substring(s.lastIndexOf(URI_CLASS_DELIM)+1, s.length()-1);
	}


	public String getObjectPropertyRange(OWLObjectPropertyExpression role) {
		
		OWLClassExpression range = role.getRanges(this.config).iterator().next();
		String s = range.toString();
		return s.substring(s.lastIndexOf(URI_CLASS_DELIM)+1, s.length()-1);
	}


	public String getDomainTableName(String column) {
		OWLObjectPropertyExpression role = this.column_role.get(column);
		return getObjectPropertyDomain(role);
	}

	
	public String getRangeTableName(String column) {
		OWLObjectPropertyExpression role = this.column_role.get(column);
		return getObjectPropertyRange(role);
	}

	
	public String getDatabaseIRI(String table) {

		return this.table_db.get(table);
	}

	
	public Set<String> getColumns(String table) {

		return this.table_column.get(table);
	}

	
	public Set<String> getTables(String dbIRI) {

		return this.db_table.get(dbIRI);
	}


	public DBConnection getDatabaseConnection(String dbIRI) {
		
		return this.db_conn.get(dbIRI);
	}
	public Set<DBConnection> getDatabaseConnections() {
		Set<DBConnection> conns = new HashSet<DBConnection>();
		conns.addAll(this.db_conn.values());
		return conns;
	}

	public OWLObjectPropertyExpression getOWLObjectProperty(String column) {
		
		return this.column_role.get(column);
	}



	
}
