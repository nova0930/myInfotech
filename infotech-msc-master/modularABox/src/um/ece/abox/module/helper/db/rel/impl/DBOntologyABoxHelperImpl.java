package um.ece.abox.module.helper.db.rel.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.semanticweb.owlapi.model.*;

import um.ece.abox.module.helper.db.rel.*;
import um.ece.abox.module.helper.memo.AbstractOntologyABoxHelper;
import um.ece.abox.module.model.ABoxModule;


public class DBOntologyABoxHelperImpl extends AbstractOntologyABoxHelper {

	DBConfiguration configuration;
	OWLOntology tbox;
	OWLDataFactory df;
	String uri;
	Map<String, OWLNamedIndividual> indCache;  // MAP<IND_DBID, OWLIND>
	Map<OWLNamedIndividual, ABoxModule> modules;
	public char DELIM = '/';
	
	
	public DBOntologyABoxHelperImpl(OWLOntology ont, DBConfiguration config) {
		this.tbox = ont;
		this.df = ont.getOWLOntologyManager().getOWLDataFactory();
		this.configuration = config;
		this.uri = tbox.getClassesInSignature(false).iterator().next().toString();
		this.uri = this.uri.substring(1, this.uri.lastIndexOf(DELIM));
		this.indCache = new HashMap<String, OWLNamedIndividual>();
		this.modules = new HashMap<OWLNamedIndividual, ABoxModule>();
		this.loadABox(null);
	}
	
	public void loadABox(String url) {
		
		// open database connections
		for (DBConnection con : this.configuration.getDatabaseConnections())
			con.connect2Database();
	}
	
//	public void loadABox(Set<String> urls) {
//		
////		if (urls.size() < 2)
////			return;
////		
////		Iterator<String> it = urls.iterator();
////		String modelURL = it.next();
////		String mappingURL = it.next();
////		this.configuration.loadDatabaseModel(modelURL);
////		this.configuration.loadDatabaseMapping(mappingURL);
////		
//		// open database connections
//		for (DBConnection con : this.configuration.dbConns.values())
//			con.connect2Database();
//	}
	
	public Set<OWLNamedIndividual> getNamedIndividuals() {
		Set<OWLNamedIndividual> inds = new HashSet<OWLNamedIndividual>();
		return inds;
	}

	
	public Set<OWLAxiom> getAssertions(AxiomType<?>... types) {
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		return result;
	}

	
	public Set<OWLAxiom> getAssertions(OWLNamedIndividual ind) {
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		if (!(ind instanceof DBOWLNamedIndividual))
			return result;
		
		DBOWLNamedIndividual dbind = (DBOWLNamedIndividual) ind;
		
		// if the individual is found in cache
		if (this.indCache.get(dbind.getDatabaseID())!=null && dbind.isFullyLoaded()) {
			result.addAll(dbind.getOWLClassAssertions());
			result.addAll(dbind.getOWLObjectPropertyAssertions());
			return result;
		}
		
		// if not found in cache, query the database
		DBConnection dbcon = this.configuration.getDatabaseConnection(dbind.getDataBaseIRI());
		String table = dbind.getDataBaseTableName();
		Set<String> columns = this.configuration.getColumns(table);
		ResultSet rs = dbcon.executeQuery(table, columns, "where ID="+dbind.getDatabaseID());
		
		try {
			while (rs.next()) {
				// for every column, create a role assertion
				for (String col : columns) {
					String objID = rs.getString(col);
					String objTable = this.configuration.getRangeTableName(col);
					String objDB = this.configuration.getDatabaseIRI(objTable);
					
					DBOWLNamedIndividual obj = (this.indCache.get(objID) != null) ?
							 					(DBOWLNamedIndividual) this.indCache.get(objID) :
							 					new DBOWLNamedIndividualImpl(this.uri, objDB, objTable, objID);
					
					OWLObjectPropertyExpression role = this.configuration.getOWLObjectProperty(col);
					OWLObjectPropertyAssertionAxiom ax = 
												this.df.getOWLObjectPropertyAssertionAxiom(role, dbind, obj);
					dbind.addOWLObjectPropertyAssertion(ax);
					obj.addOWLObjectPropertyAssertion(ax);
					result.add(ax);
					// save the object to cache
					this.indCache.put(objID, obj);
				} // end of for
			} // end of while
			
			dbind.setFullyLoaded();
			this.indCache.put(dbind.getDatabaseID(), dbind);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		result.addAll(dbind.getOWLClassAssertions());
		return result;
	}

	
	public Set<OWLAxiom> getAssertions(OWLNamedIndividual ind, AxiomType<?>... type) {
		
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		Set<AxiomType<?>> types = new HashSet<AxiomType<?>>();
		for (AxiomType<?> t : type)
			types.add(t);
		
		Set<OWLAxiom> axioms = this.getAssertions(ind);
		for (OWLAxiom ax : axioms) 
			if (types.contains(ax.getAxiomType()))
				result.add(ax);
		
		return result;
	}

	
	public Set<OWLNamedIndividual> getNeighbors(OWLNamedIndividual ind) {

		Set<OWLNamedIndividual> result = new HashSet<OWLNamedIndividual>();
		this.getAssertions(ind);
		for (OWLIndividual in : ((DBOWLNamedIndividual)ind).getObjectPropertyValues())
			result.add(in.asOWLNamedIndividual());
		return result;
	}

	
	public Set<OWLNamedIndividual> getNeighbors(OWLNamedIndividual ind, OWLObjectPropertyExpression role) {
		
		Set<OWLNamedIndividual> result = new HashSet<OWLNamedIndividual>();
		this.getAssertions(ind);
		for (OWLIndividual in : ((DBOWLNamedIndividual)ind).getObjectPropertyValues(role))
			result.add(in.asOWLNamedIndividual());
		return result;
	}

	
	public Set<OWLClassExpression> getClasses(OWLNamedIndividual ind) {
		
		return ((DBOWLNamedIndividual)ind).getTypes();
	}

	
	public ABoxModule getConnectedComponent(OWLNamedIndividual ind) {
		
		if (this.modules.get(ind) != null)
			return this.modules.get(ind);
		
		ABoxModule m = super.getConnectedComponent(ind);
		for (OWLNamedIndividual in : m.getSignature()) 
			this.modules.put(in, m);
		
		return m;
	}

	
	public Set<ABoxModule> getConnectedComponents() {
		Set<ABoxModule> m = new HashSet<ABoxModule>();
		return m;
	}


	

}
