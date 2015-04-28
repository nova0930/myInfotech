package um.ece.abox.module.helper.db.titan;

import org.semanticweb.owlapi.model.*;

import um.ece.abox.module.helper.AbstractOWLNamedIndividual;

public class TitanNamedIndividual extends AbstractOWLNamedIndividual{

	private static final long serialVersionUID = 3761970418474317240L;
	Object titanID;
	boolean isloaded;
	Iterable<OWLAxiom> classAssertions;
	Iterable<OWLAxiom> roleAssertions;
	
	public TitanNamedIndividual(OWLDataFactory df, Object id, PrefixManager pm) {
		// create a named individual here.
		super(AbstractOWLNamedIndividual.create(df, id+"", pm));
		this.titanID = id;
		this.isloaded = false;
	}
	
	
	public Object getId() {
		return this.titanID;
	}
	
	public boolean isLoaded() {
		return this.isloaded;
	}
	
	public void setLoaded() {
		this.isloaded = true;
	}
	
	public void setClassAssertions(Iterable<OWLAxiom> axs) {
		this.classAssertions = axs;
	}
	
	public Iterable<OWLAxiom> getClassAssertions() {
		return this.classAssertions;
	}
	
	public void setRoleAssertions(Iterable<OWLAxiom> axs) {
		this.roleAssertions = axs;
	}
	
	public Iterable<OWLAxiom> getRoleAssertions() {
		return this.roleAssertions;
	}
	
}
