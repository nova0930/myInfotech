package com.infotechsoft.integration.queryplan.semqa;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.sparql.core.Var;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;
import com.infotechsoft.integration.queryplan.semqa.utils.NodeUtils;


public class TriplePattern implements GraphPattern {

	private Node subject;
	private Node property;
	private Node object;
	private Set<Node_URI> terms = new LinkedHashSet<Node_URI>();
	private Set<Var> varsInOps = new LinkedHashSet<Var>();

	public TriplePattern(Node subject, Node property, Node object) {
		super();
		this.subject = subject;
		this.property = property;
		this.object = object;
		addIfURI(terms, subject);
		addIfURI(terms, property);
		addIfURI(terms, object);
		addIfVar(varsInOps, subject);
		addIfVar(varsInOps, property);
		addIfVar(varsInOps, object);
	}
	public Node getSubject() {
		return subject;
	}
	public void setSubject(Node subject) {
		this.subject = subject;
	}
	public Node getProperty() {
		return property;
	}
	public void setProperty(Node property) {
		this.property = property;
	}
	public Node getObject() {
		return object;
	}
	public void setObject(Node object) {
		this.object = object;
	}
	private void addIfURI(Set<Node_URI> termSet, Node e) {
		if (e instanceof Node_URI) {
			termSet.add((Node_URI) e);
		}
	}
	private void addIfVar(Set<Var> varSet, Node e) {
		if (e instanceof Var) {
			varSet.add((Var) e);
		}
	}
	@Override
	public Set<Node_URI> getUriMentioned() {
		return terms;
	}
	@Override
	public Set<Var> getVarsMentioned() {
		return varsInOps; // all variables mentioned are in operands - in itself, in this case
	}
	@Override
	public Set<Var> getVarsMentionedInOperands() {
		return varsInOps;
	}
	@Override
	public boolean equals(Object obj) {
		return ((obj instanceof TriplePattern) &&
				(this.subject.equals(((TriplePattern) obj).getSubject())) &&
				(this.property.equals(((TriplePattern) obj).getProperty())) &&
				(this.object.equals(((TriplePattern) obj).getObject()))
				);
	}
	@Override
	public int hashCode() {
		// use method from Effective Java (2001)
		int result = QAOperation.SEED;
		result = QAOperation.PRIME_NUMBER * result + subject.hashCode();
		result = QAOperation.PRIME_NUMBER * result + property.hashCode();
		result = QAOperation.PRIME_NUMBER * result + object.hashCode();
		return result;
	}
	@Override
	public String toString() {
		return "{ "+this.subject.toString() + " " + this.property.toString() + " " + this.object.toString()+ " }";
	}
	@Override
	public GraphPattern copy() throws SemQAException {
		return new TriplePattern(
				NodeUtils.copyNode(subject), 
				NodeUtils.copyNode(property), 
				NodeUtils.copyNode(object));
	}
	@Override
	public boolean contains(GraphPattern gp) {
		// a triple pattern can only contain another if they are the same
		return (this.equals(gp));
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new TriplePattern(this.subject, this.property, this.object);
	}

}
