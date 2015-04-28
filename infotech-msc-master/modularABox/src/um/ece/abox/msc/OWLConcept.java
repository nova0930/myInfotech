package um.ece.abox.msc;

import org.semanticweb.owlapi.model.OWLClassExpression;

public class OWLConcept implements java.io.Serializable {
	private static final long serialVersionUID = -4638299903546724553L;
	OWLClassExpression concept;
	int roleDepth;
	int andBranches;
	
	public OWLConcept(OWLClassExpression c, int rd, int ab) {
		this.concept = c;
		this.roleDepth = rd;
		this.andBranches = ab;
	}
	
	public OWLClassExpression getConcept() {
		return concept;
	}
	
	public int getRoleDepth() {
		return roleDepth;
	}
	
	public int getAndBranches() {
		return andBranches;
	}
}
