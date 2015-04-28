package com.infotechsoft.integration.queryplan.semqa.msc;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import com.hp.hpl.jena.graph.Node;
import com.infotechsoft.integration.queryplan.semqa.GraphPattern;
import com.infotechsoft.integration.queryplan.semqa.Join;
import com.infotechsoft.integration.queryplan.semqa.TriplePattern;

public class SemqaMSC {
	
	public OWLClassExpression getMsc(GraphPattern gp, Node n) {
		
		Set<TriplePattern> triples = new HashSet<TriplePattern>();
		
		if (gp instanceof Join)
			for (GraphPattern p : ((Join) gp).getOperands())
				triples.add((TriplePattern) p);
		else if (gp instanceof TriplePattern)
				triples.add((TriplePattern) gp);
		//TODO handle other situations. 
	
		OWLDataFactory df = OWLManager.getOWLDataFactory();
		TriplePatternHelper sh = new TriplePatternHelper(triples, df);
		TriplePatternMSC msc = new TriplePatternMSC(df, sh);
		
		return msc.getMsc(n);
	}

}
