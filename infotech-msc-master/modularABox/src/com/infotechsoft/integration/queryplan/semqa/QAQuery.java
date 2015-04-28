package com.infotechsoft.integration.queryplan.semqa;

import java.util.Set;

import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.sparql.core.Var;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;


public class QAQuery implements GraphPattern {

	private GraphPattern graphPattern;
	public QAQuery(GraphPattern graphPattern) {
		super();
		this.graphPattern = graphPattern;
	}
	public GraphPattern getGraphPattern() {
		return graphPattern;
	}
	public void setGraphPattern(GraphPattern graphPattern) {
		this.graphPattern = graphPattern;
	}
	@Override
	public Set<Node_URI> getUriMentioned() {
		return graphPattern.getUriMentioned();
	}
	@Override
	public Set<Var> getVarsMentioned() {
		return graphPattern.getVarsMentioned();
	}
	@Override
	public Set<Var> getVarsMentionedInOperands() {
		return graphPattern.getVarsMentionedInOperands();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof QAQuery) {
			QAQuery otherquery = (QAQuery) obj;
			return (graphPattern == null) ? 
					(otherquery.getGraphPattern() == null) :
					(graphPattern.equals(otherquery.getGraphPattern()));
		}
		else {
			return false;
		}
	}
	@Override
	public int hashCode() {
		// use method from Effective Java (2001)
		int result = QAOperation.SEED;
		result = QAOperation.PRIME_NUMBER * result + graphPattern.hashCode();
		return result;
	}
	@Override
	public String toString() {
		if (graphPattern == null) {
			return("NULL QUERY");
		}
		else {
			return graphPattern.toString();
		}
	}
	@Override
	public GraphPattern copy() throws SemQAException {
		if (graphPattern == null) {
			return new QAQuery(null);
		}
		else {
			return new QAQuery(graphPattern.copy());
		}
	}
	@Override
	public boolean contains(GraphPattern gp) {
		if (this.graphPattern == null) {
			return (gp == null);
		}
		else {
			return this.graphPattern.contains(gp);
		}
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new QAQuery(this.graphPattern);
	}

}
