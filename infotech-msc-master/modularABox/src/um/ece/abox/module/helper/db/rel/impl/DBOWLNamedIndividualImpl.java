package um.ece.abox.module.helper.db.rel.impl;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import um.ece.abox.module.helper.AbstractOWLNamedIndividual;
import um.ece.abox.module.helper.db.rel.DBOWLNamedIndividual;

public class DBOWLNamedIndividualImpl extends AbstractOWLNamedIndividual implements DBOWLNamedIndividual{
	
	private static final long serialVersionUID = 4217103158762554163L;
	boolean isloaded;		// indicate whether data of this individual has been 
							// fully loaded from the database.
	OWLNamedIndividual ind;	// OWL object representation of the data item
	Set<OWLAxiom> classAssertions;
	Set<OWLAxiom> roleAssertions;
	String databaseIRI,   	// the DB OWL instance IRI in the RDF mapping ontology.
			tableName,		// the DB table name where the data item resides
			dbID; 		// the ID of the underlying data item in the database table 
			
	public DBOWLNamedIndividualImpl(String uri, String dbIRI, String table, String id) {
		
		super(OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(":"+id, new DefaultPrefixManager(uri)));
		
		uri = uri.endsWith("/") ? uri : uri + "/";
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		PrefixManager pm = new DefaultPrefixManager(uri);

		this.ind = df.getOWLNamedIndividual(":"+id, pm);
		this.databaseIRI = dbIRI;
		this.tableName = table;
		this.dbID = id;
		this.classAssertions = new HashSet<OWLAxiom>();
		this.roleAssertions = new HashSet<OWLAxiom>();
		this.isloaded = false;
		
		// declare the class assertion
		OWLClass c = df.getOWLClass(":"+table, pm);
		this.classAssertions.add(df.getOWLClassAssertionAxiom(c, ind));
	}
	
	public void addOWLClassAssertion(OWLClassAssertionAxiom ax) {
		if (ax.getIndividual().equals(this.ind))
			this.classAssertions.add(ax);
	}
	
	public void addOWLClassAssertions(Set<OWLClassAssertionAxiom> axs) {
		for (OWLClassAssertionAxiom ax : axs)
			this.addOWLClassAssertion(ax);
	}
	
	public void addOWLObjectPropertyAssertion(OWLObjectPropertyAssertionAxiom ax) {
		if (ax.getSubject().equals(this.ind)
				|| ax.getObject().equals(this.ind))
			this.roleAssertions.add(ax);
	}
	
	public void addOWLObjectPropertyAssertions(Set<OWLObjectPropertyAssertionAxiom> axs) {
		for (OWLObjectPropertyAssertionAxiom ax : axs) 
			this.addOWLObjectPropertyAssertion(ax);
	}

	
	public Set<OWLIndividual> getObjectPropertyValues() {
		
		Set<OWLIndividual> result = new HashSet<OWLIndividual>();
		
		for (OWLAxiom ass : this.roleAssertions) {
			OWLObjectPropertyAssertionAxiom rass = (OWLObjectPropertyAssertionAxiom)ass;
//			OWLObjectPropertyExpression role = rass.getProperty();
			OWLIndividual sub = rass.getSubject();
			OWLIndividual obj = rass.getObject();
			OWLIndividual neighbor = (sub.equals(this.ind)) ? obj : sub;
			result.add(neighbor);
		}
		return result;
	}

	
	public Set<OWLIndividual> getObjectPropertyValues(OWLObjectPropertyExpression arg0) {
		
		Set<OWLIndividual> result = new HashSet<OWLIndividual>();
		for (OWLAxiom ass : this.roleAssertions) {
			OWLObjectPropertyAssertionAxiom rass = (OWLObjectPropertyAssertionAxiom)ass;
			OWLObjectPropertyExpression role = rass.getProperty();
			OWLIndividual sub = rass.getSubject();
			OWLIndividual obj = rass.getObject();
			OWLIndividual neighbor = (sub.equals(this.ind)) ? obj : sub;
			OWLObjectPropertyExpression r = (sub.equals(this.ind)) ? role : 
												role.getInverseProperty().getSimplified();
			if (r.equals(arg0))
				result.add(neighbor);
		}
		return result;
	}

	
	
	public Set<OWLClassExpression> getTypes() {
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		for (OWLAxiom ax : this.classAssertions) 
			result.add(((OWLClassAssertionAxiom)ax).getClassExpression());
		return result;
	}


	public Set<OWLAxiom> getOWLClassAssertions() {

		return this.classAssertions;
	}


	public Set<OWLAxiom> getOWLObjectPropertyAssertions() {
		
		return this.roleAssertions;
	}


	public String getDataBaseIRI() {
		
		return this.databaseIRI;
	}


	public String getDataBaseTableName() {
		
		return this.tableName;
	}
	
	public String getDatabaseID(){
		
		return this.dbID;
	}


	public boolean isFullyLoaded() {
		return this.isloaded;
	}


	public void setFullyLoaded() {
		this.isloaded = true;
	}
	
	public Object getId() {
		return dbID;
	}

}
