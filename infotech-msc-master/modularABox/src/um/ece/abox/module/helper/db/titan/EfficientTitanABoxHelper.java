package um.ece.abox.module.helper.db.titan;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import um.ece.abox.module.helper.AbstractOWLClassAssertionAxiom;

import com.google.common.collect.Iterables;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class EfficientTitanABoxHelper extends TitanABoxHelper {

	public EfficientTitanABoxHelper(OWLDataFactory df, String dataPrefix, String confFile) {
		super(df, dataPrefix, confFile);
	}

	public EfficientTitanABoxHelper(OWLDataFactory df, String dataPrefix, Configuration conf) {
		super(df, dataPrefix, conf);
	}
	

	/**
	 * Support Class assertion and Object property assertion.
	 */
//	@SuppressWarnings("unchecked")
	public Iterable<OWLAxiom> getAssertions(OWLNamedIndividual ind, AxiomType<?>... types) {
		
		loadIndividual(ind);
		TitanNamedIndividual tind = (TitanNamedIndividual) ind;
		Iterable<OWLAxiom> assertions = new HashSet<OWLAxiom>();
		
		for (AxiomType<?> t : types) {
			if (t.equals(AxiomType.CLASS_ASSERTION)) 
				assertions = Iterables.concat(assertions, tind.getClassAssertions());
			else if (t.equals(AxiomType.OBJECT_PROPERTY_ASSERTION)) 
				assertions = Iterables.concat(assertions, tind.getRoleAssertions());
		}
		
		return assertions;
	}

	public Iterable<OWLNamedIndividual> getNeighbors(OWLNamedIndividual ind) {
		
		loadIndividual(ind);
		TitanNamedIndividual tind = (TitanNamedIndividual) ind;
		Set<OWLNamedIndividual> neighbors = new HashSet<OWLNamedIndividual>();

		for (OWLAxiom ax : tind.getRoleAssertions()) {
			OWLObjectPropertyAssertionAxiom ass = (OWLObjectPropertyAssertionAxiom) ax;
			neighbors.add(this.getNeighbor(ass, tind));
		}
		return neighbors;
	}

	public Iterable<OWLNamedIndividual> getNeighbors(OWLNamedIndividual ind, OWLObjectPropertyExpression role) {
		
		loadIndividual(ind);
		TitanNamedIndividual tind = (TitanNamedIndividual) ind;
		Set<OWLNamedIndividual> neighbors = new HashSet<OWLNamedIndividual>();
		
		for (OWLAxiom ax : tind.getRoleAssertions()) {
			OWLObjectPropertyAssertionAxiom ass = (OWLObjectPropertyAssertionAxiom) ax;
			if (this.getDirectedRole(ass, tind).equals(role))
				neighbors.add(this.getNeighbor(ass, tind));
		}
		return neighbors;
	}

	public Set<OWLClassExpression> getClasses(OWLNamedIndividual ind) {
		
		loadIndividual(ind);
		TitanNamedIndividual tind = (TitanNamedIndividual) ind;
		
		Set<OWLClassExpression> classes = new HashSet<OWLClassExpression>();
		Iterable<OWLAxiom> class_ax = tind.getClassAssertions();
		
		for (OWLAxiom ax : class_ax) {
			OWLClassAssertionAxiom ass = (OWLClassAssertionAxiom) ax;
			classes.add(ass.getClassExpression());
		}
		
		return classes;
	}
	
	
	public void loadIndividual(Object indId) {
		loadIndividual(new TitanNamedIndividual(dataFactory, indId, prefixManager));
	}
	
	public void loadIndividual(OWLNamedIndividual ind) {
		
		TitanNamedIndividual tind = (TitanNamedIndividual) ind;
		if (tind.isLoaded())
			return;
		
		//System.out.println("Haha Hbase is accessed.");
		Set<OWLAxiom> class_ax = new HashSet<OWLAxiom>();
		Iterable<OWLAxiom> role_ax = new HashSet<OWLAxiom>();
		Vertex v = this.graph.getVertex(tind.getId());
		// load  class assertion
		String cls = v.getProperty("type");
		OWLClassExpression c = this.dataFactory.getOWLClass(cls, prefixManager);
		OWLClassAssertionAxiom ax = this.dataFactory.getOWLClassAssertionAxiom(c, tind);
		class_ax.add(new AbstractOWLClassAssertionAxiom(ax));
		// load role assertions
		Iterable<Edge> edges = Iterables.concat(v.getEdges(Direction.IN), v.getEdges(Direction.OUT));
		role_ax = new TitanAxiomIterable(edges, prefixManager, dataFactory);
		
		tind.setClassAssertions(class_ax);
		tind.setRoleAssertions(role_ax);
		tind.setLoaded();
	}
	

}
