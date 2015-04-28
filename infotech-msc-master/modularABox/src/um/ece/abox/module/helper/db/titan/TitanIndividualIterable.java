package um.ece.abox.module.helper.db.titan;

import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.PrefixManager;

import com.tinkerpop.blueprints.Vertex;

public class TitanIndividualIterable implements Iterable<OWLNamedIndividual> {
	
	Iterable<Vertex> data;
	PrefixManager prefixManager;
	OWLDataFactory df;
	
	public TitanIndividualIterable(Iterable<Vertex> data, PrefixManager pm, OWLDataFactory df) {
		this.data = data;
		this.prefixManager = pm;
		this.df = df;
	}
	
	public Iterator<OWLNamedIndividual> iterator() {
		return new TitanIndividualIterator(data.iterator());
	}
	
	class TitanIndividualIterator implements Iterator<OWLNamedIndividual> {
		
		Iterator<Vertex> src;
		
		public TitanIndividualIterator(Iterator<Vertex> source) {
			this.src = source;
		}
		
		public boolean hasNext() {
			return src.hasNext();
		}

		public OWLNamedIndividual next() {
			Vertex v = src.next();
			return new TitanNamedIndividual(df, v.getId(), prefixManager);
		}

		public void remove() {
			src.remove();
		}
		
	}

}
