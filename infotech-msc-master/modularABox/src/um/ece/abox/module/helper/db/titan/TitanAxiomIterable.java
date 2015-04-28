package um.ece.abox.module.helper.db.titan;

import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.PrefixManager;

import um.ece.abox.module.helper.AbstractOWLObjectPropertyAssertionAxiom;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;

public class TitanAxiomIterable implements Iterable<OWLAxiom> {

	Iterable<Edge> data;
	PrefixManager prefixManager;
	OWLDataFactory df;
	
	public TitanAxiomIterable(Iterable<Edge> data, PrefixManager pm, OWLDataFactory df) {
		this.data = data;
		this.prefixManager = pm;
		this.df = df;
	}
	
	public Iterator<OWLAxiom> iterator() {
		return new TitanAxiomIterator(data.iterator());
	}
	
	class TitanAxiomIterator implements Iterator<OWLAxiom> {
		
		Iterator<Edge> src;
		
		public TitanAxiomIterator(Iterator<Edge> source) {
			this.src = source;
		}
		
		public boolean hasNext() {
			return src.hasNext();
		}

		public OWLAxiom next() {
			Edge e = src.next();
			TitanNamedIndividual sub = new TitanNamedIndividual(df, e.getVertex(Direction.OUT).getId(), prefixManager);
			TitanNamedIndividual obj = new TitanNamedIndividual(df, e.getVertex(Direction.IN).getId(), prefixManager);
			OWLObjectPropertyExpression role = df.getOWLObjectProperty(e.getLabel(), prefixManager);
			//System.out.println("HAHA ObjectPropertyExpression: " + sub + " " + role + " " + obj);
			return new AbstractOWLObjectPropertyAssertionAxiom(df.getOWLObjectPropertyAssertionAxiom(role, sub, obj));
		}

		public void remove() {
			src.remove();
		}
		
	}

}
