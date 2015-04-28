package um.ece.abox.module.helper.memo;


import org.semanticweb.owlapi.model.*;

import um.ece.abox.module.helper.AbstractOWLNamedIndividual;

public class MemoNamedIndividual extends AbstractOWLNamedIndividual{

	private static final long serialVersionUID = 3761970418474317240L;
	boolean isloaded;
	String ID;
	Iterable<OWLAxiom> classAssertions;
	Iterable<OWLAxiom> roleAssertions;
	
	public MemoNamedIndividual(OWLDataFactory df, String id, PrefixManager pm) {
		// create a named individual here.
		super(AbstractOWLNamedIndividual.create(df, id+"", pm));
		this.ID = id;
		
	}
	
	
	
	public String getId() {
		return this.ID;
	}

	public static OWLNamedIndividual create(OWLDataFactory df, String name, PrefixManager pm) {
		return df.getOWLNamedIndividual(name, pm);
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
