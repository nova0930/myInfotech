package um.ece.spark.cluster.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import um.ece.abox.module.condition.SyntacticConditionChecker;

import um.ece.abox.module.helper.memo.*;
import um.ece.abox.module.parser.ontology.SHIQOntologyParser;
import um.ece.abox.msc.SHIMscExtractor;


public class MemoMSC {
	
									            
	
	public Set<OWLNamedIndividual> runMsc(final String ontPrefix, final OWLOntology tbox,  final OWLOntology abox,
			final OWLClassExpression query)
		{ 
		
		Set<OWLNamedIndividual> inds = new HashSet<OWLNamedIndividual>();
		//Iterator<String>, OWLNamedIndividual>
		
			
			System.out.println("Call runMSC methods");
				long startTime = System.currentTimeMillis();
				
				// initializing TBox analyzer and MSC calculator
				DefaultPrefixManager prefixManager = new DefaultPrefixManager(ontPrefix);
		        OWLDataFactory df = OWLManager.getOWLDataFactory();
		        //There should be normal ABox helper
		        SHIQOntologyABoxHelper aboxHelper = new SHIQOntologyABoxHelper(abox);
		        SHIQOntologyRBoxHelper rboxHelper = new SHIQOntologyRBoxHelper(tbox);
		        SyntacticConditionChecker condChecker = new SyntacticConditionChecker(tbox, aboxHelper, rboxHelper);
		        SHIQOntologyParser ontParser = new SHIQOntologyParser(tbox);
		        ontParser.parseOntology();
		        ontParser.accept(condChecker);
		        SHIMscExtractor msc = new SHIMscExtractor(aboxHelper, condChecker, df);
		        OWLReasoner reasoner = condChecker.getReasoner();
						  
		        System.out.println("Initiation Time :\t" + (System.currentTimeMillis() - startTime));
		        
		        
		        ArrayList<OWLNamedIndividual> result = new ArrayList<OWLNamedIndividual>();
		        // use MSC for instance checking
		        Iterator<OWLNamedIndividual> t = result.iterator();
		        
		        if(t.hasNext())
		        	System.out.println("It has individuals");
		        else 
		        	System.out.println("No individuals");
		        
		    
				while (t.hasNext()) {
					String id = t.next().toString();
					startTime = System.currentTimeMillis();
					
					System.out.println("The current individual "+id);
					OWLNamedIndividual ind = new MemoNamedIndividual(df, id, prefixManager);
					OWLClassExpression cls = msc.getMSC(ind); 
				    long mscTime = System.currentTimeMillis() - startTime;
				    startTime = System.currentTimeMillis();
				   
				    OWLAxiom ax = df.getOWLSubClassOfAxiom(cls, query);
				    boolean b = reasoner.isEntailed(ax);
				    long reasoningTime = System.currentTimeMillis() - startTime;
				    System.out.println(mscTime + "\t" + reasoningTime);
				    
				
				    if (b) inds.add((OWLNamedIndividual) ind);   
			
				}
		
		
	
		return inds;	
}
}
