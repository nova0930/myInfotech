package main;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;


import um.ece.abox.module.condition.SyntacticConditionChecker;
import um.ece.abox.module.helper.memo.SHIQOntologyRBoxHelper;
import um.ece.abox.module.parser.ontology.SHIQOntologyParser;
import um.ece.abox.msc.SHIMscExtractor;
import um.ece.abox.module.helper.OntologyRBoxHelper;
import um.ece.abox.module.helper.memo.*;
import um.ece.abox.module.parser.ontology.OntologyParser;


public class TestMemo {
	
	public static void main(String args[]) throws OWLOntologyCreationException {
		
		
		//String prefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl";
		String tfile = "/home/zhangda/Desktop/Infotech/univ.owl"; 
		String afile = "/home/zhangda/Desktop/Infotech/lubm1.owl";
        OWLDataFactory df = OWLManager.getOWLDataFactory();
		
		OWLOntology tbox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(tfile));
		OWLOntology abox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(afile));
		SHIQOntologyABoxHelper ah = new SHIQOntologyABoxHelper(abox); 
		OntologyRBoxHelper rh = new SHIQOntologyRBoxHelper(tbox);
		SyntacticConditionChecker condChecker = new SyntacticConditionChecker(tbox, ah, rh);
		OntologyParser ontParser = new SHIQOntologyParser(tbox);
		
		  ontParser.parseOntology();
	      ontParser.accept(condChecker);
	      SHIMscExtractor msc = new SHIMscExtractor(ah, condChecker, df);
	      OWLReasoner reasoner = condChecker.getReasoner();
	      Set<OWLNamedIndividual> inds = new HashSet<OWLNamedIndividual>();
		
		Object[] concepts = tbox.getClassesInSignature().toArray();
		int i =0;
		
		for (i =0; i<=concepts.length-1;i++)
		{
			System.out.println("Number: "+i+ " "+concepts[i]);
		}
		
		System.out.println("After Analyzing Tbox, Thereare "+i+" Classes");
		
		
		int maxDepth = 0, maxWidth = 0,num = 0;
		
		
		for(int classes = 0; classes<concepts.length;classes++)	{
			
		OWLClassExpression query = (OWLClassExpression) concepts[classes];
		System.out.println("The query is  : "+query);
		
		int test = 0;
		
	
		
		
		for (OWLNamedIndividual ind : ah.getNamedIndividuals()) {
			//num++;
			ah.getClasses(ind);
			
			OWLClassExpression cls = msc.getMSC(ind); 
			System.out.println("The MSC concept for ind "+ind+"is  : "+ah.getClasses(ind));
			OWLAxiom ax = df.getOWLSubClassOfAxiom(cls, query);
			
			boolean b = reasoner.isEntailed(ax);
		    
		
		    if (b) 
		    	{
		    	//System.out.println("The included individual is : "+ind);
		    	//num++;
		    	if (!inds.contains((OWLNamedIndividual) ind))
		    	inds.add((OWLNamedIndividual) ind);
		    	
		    	}

		}
		
	
		
		//System.out.println(num);
		System.out.println("Max Depth " + maxDepth);
		System.out.println("Max Width " + maxWidth);
		System.out.println("The Number of individuals size is  "+inds.size());
		
		
	}
	
}
	

}
