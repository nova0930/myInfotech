package um.ece.abox.module.helper.db.titan;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanType;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import um.ece.abox.module.helper.AbstractOWLClassAssertionAxiom;
import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.model.ABoxModule;

import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;
import com.thinkaurelius.titan.graphdb.internal.InternalVertex;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;

public class TitanABoxHelper implements OntologyABoxHelper {
	
	//OWLOntology tbox;
	TitanGraph graph;
	OWLDataFactory dataFactory;
	String uriPrefix;
	PrefixManager prefixManager;
	public char DELIM = '#';
	
	public TitanABoxHelper(OWLDataFactory df, String dataPrefix, String confFile) {
		this.graph = TitanFactory.open(confFile);
		init(df, dataPrefix);
	}
	
	public TitanABoxHelper(OWLDataFactory df, String dataPrefix, Configuration conf) {
		this.graph = TitanFactory.open(conf);
		init(df, dataPrefix);
	}
	
	private void init(OWLDataFactory df, String dataPrefix) {
		this.dataFactory = df;
		this.uriPrefix = dataPrefix;
		this.prefixManager = new DefaultPrefixManager(uriPrefix);
	}

	/** Do nothing. Entire ABox has no way to be loaded into memory.*/
	public void loadABox(String url) {}

	public Iterable<OWLNamedIndividual> getNamedIndividuals() {
		return new TitanIndividualIterable(graph.getVertices(), prefixManager, dataFactory);
	}
	
	public Iterator<Long> getNamedIndividualIDs() {
		StandardTitanGraph g = (StandardTitanGraph) graph;
		StandardTitanTx tx = (StandardTitanTx) g.getCurrentThreadTx();
		return g.getVertexIDs(tx.getTxHandle());
	}

	/**
	 * Support only Object property assertion of the whole ABox.
	 */
	public Iterable<OWLAxiom> getAssertions(AxiomType<?>... types) {
		return new TitanAxiomIterable(graph.getEdges(), prefixManager, dataFactory);
	}

	/**
	 * Support Class assertion and Object property assertion.
	 */
	public Iterable<OWLAxiom> getAssertions(OWLNamedIndividual ind) {
		
		return this.getAssertions(ind, AxiomType.CLASS_ASSERTION, AxiomType.OBJECT_PROPERTY_ASSERTION);
	}

	/**
	 * Support Class assertion and Object property assertion.
	 */
//	@SuppressWarnings("unchecked")
	public Iterable<OWLAxiom> getAssertions(OWLNamedIndividual ind, AxiomType<?>... types) {
		
		Set<OWLAxiom> class_ax = new HashSet<OWLAxiom>();
		Iterable<OWLAxiom> role_ax = new HashSet<OWLAxiom>();
		TitanNamedIndividual tind = (TitanNamedIndividual) ind;
		Vertex v = this.graph.getVertex(tind.getId());
		if (v == null) 
			return class_ax;
		
		for (AxiomType<?> t : types) {
			if (t.equals(AxiomType.CLASS_ASSERTION)) {
				String cls = v.getProperty("type");
				if (cls == null) continue;
				OWLClassExpression c = this.dataFactory.getOWLClass(cls, prefixManager);
				OWLClassAssertionAxiom ax = this.dataFactory.getOWLClassAssertionAxiom(c, tind);
				class_ax.add(new AbstractOWLClassAssertionAxiom(ax));
			} else if (t.equals(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
				Iterable<Edge> edges = Iterables.concat(v.getEdges(Direction.IN), v.getEdges(Direction.OUT));
				role_ax = new TitanAxiomIterable(edges, prefixManager, dataFactory);
			}
		}
		
		return Iterables.concat(class_ax, role_ax);
	}

	public Iterable<OWLNamedIndividual> getNeighbors(OWLNamedIndividual ind) {

		TitanNamedIndividual tind = (TitanNamedIndividual) ind;
		Vertex v = this.graph.getVertex(tind.getId());
		if (v == null) 
			return new HashSet<OWLNamedIndividual>();
		
		Iterable<Vertex> neighbors = Iterables.concat(v.getVertices(Direction.IN), v.getVertices(Direction.OUT)) ; 
		return new TitanIndividualIterable(neighbors, prefixManager, dataFactory);
	}

	public Iterable<OWLNamedIndividual> getNeighbors(OWLNamedIndividual ind, OWLObjectPropertyExpression role) {
		
		TitanNamedIndividual tind = (TitanNamedIndividual) ind;
		Vertex v = this.graph.getVertex(tind.getId());
		if (v == null) 
			return new HashSet<OWLNamedIndividual>();
		
		if (role.isAnonymous()) {
			role = role.getNamedProperty();
			String iri = role.toString();
			String label = iri.substring(iri.lastIndexOf(this.DELIM)+1, iri.length()-1);
			return new TitanIndividualIterable(v.getVertices(Direction.IN, label), prefixManager, dataFactory);
		} else {
			String iri = role.toString();
			String label = iri.substring(iri.lastIndexOf(this.DELIM)+1, iri.length()-1);
			return new TitanIndividualIterable(v.getVertices(Direction.OUT, label), prefixManager, dataFactory);
		}
	}

	
	public Set<OWLClassExpression> getClasses(OWLNamedIndividual ind) {
		
		Set<OWLClassExpression> classes = new HashSet<OWLClassExpression>();
		TitanNamedIndividual tind = (TitanNamedIndividual) ind;
		Vertex v = this.graph.getVertex(tind.getId());
		if (v == null)
			return classes;
		
		String t = v.getProperty("type");
		if (t != null) {
			OWLClassExpression c = this.dataFactory.getOWLClass(t, prefixManager);
			classes.add(c);
		}
		return classes;
	}

	public ABoxModule getConnectedComponent(OWLNamedIndividual ind) {
		return null;
	}

	public Set<ABoxModule> getConnectedComponents() {
		return null;
	}

	public OWLObjectPropertyExpression getDirectedRole(OWLObjectPropertyAssertionAxiom ax, OWLNamedIndividual ind) {
		OWLIndividual sub = ax.getSubject();
		OWLObjectPropertyExpression ob = ax.getProperty();
		return (sub.equals(ind)) ? ob : ob.getInverseProperty();
	}

	public OWLNamedIndividual getNeighbor(OWLObjectPropertyAssertionAxiom ax, OWLNamedIndividual ind) {
		OWLIndividual sub = ax.getSubject();
		OWLIndividual obj = ax.getObject(); 
		return (sub.equals(ind)) ? obj.asOWLNamedIndividual() : sub.asOWLNamedIndividual();
	}
	
	public Vertex getVertex(long id) {
		StandardTitanTx tx = (StandardTitanTx) graph.newTransaction();
		InternalVertex v = tx.getExistingVertex(id);
		if (v.isRemoved() || (v instanceof  TitanType)) 
			v = null;
		
		return v;
	}

}
