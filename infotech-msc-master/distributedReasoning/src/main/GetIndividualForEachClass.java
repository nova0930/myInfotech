package main;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
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


public class GetIndividualForEachClass {
	
	public static void main(String args[]) throws OWLOntologyCreationException {
		
		
		//String prefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl";
		String tfile = "/home/zhangda/Desktop/Infotech/univ.owl"; 
		String afile = "/home/zhangda/Desktop/Infotech/lubm1.owl";
        OWLDataFactory df = OWLManager.getOWLDataFactory();
		
		OWLOntologyManager tbox = OWLManager.createOWLOntologyManager();
		OWLOntologyManager abox = OWLManager.createOWLOntologyManager();
		OWLDataFactory Tdatafactory = tbox.getOWLDataFactory();
		OWLDataFactory Adatafactory = abox.getOWLDataFactory();
		
		File inputTbox = new File(tfile);
		File inputAbox = new File(afile);
		
		OWLOntology Aboxontology = abox.loadOntologyFromOntologyDocument(inputAbox);
		OWLOntology Tboxontology = tbox.loadOntologyFromOntologyDocument(inputTbox);
		
		Reasoner reasoner = new Reasoner(Aboxontology);
		Reasoner reasoner2 = new Reasoner(Tboxontology);
		
	    Set<OWLClass> owlclasses = Tboxontology.getClassesInSignature();
	    //owlclasses.size();
	    
	    Set<OWLNamedIndividual> individuals = new HashSet<OWLNamedIndividual>();
	    
	    int num = 0,count = 0;
	    for (OWLClass c: owlclasses)
	    {   count++;
	    	NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(c, true);
	    	System.out.println("The class is "+ c);
	    	for (OWLNamedIndividual i : instances.getFlattened()) {
	    		
	    		if (!individuals.contains(i))
	    		{
	    			individuals.add(i);
	    		}
	    
                
	    }
		
		
	    	System.out.println("The total number of class is"+  individuals.size());
	    	System.out.println("The total number of individual is "+  num);
		

		
		
	}
	

	

	}
}
