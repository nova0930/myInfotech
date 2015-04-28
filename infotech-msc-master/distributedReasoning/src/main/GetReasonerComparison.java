 package main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

//import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

//import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class GetReasonerComparison {
	public static void main(String args[]) throws OWLOntologyCreationException{
		
	  OWLOntologyManager m=OWLManager.createOWLOntologyManager();
	  OWLOntology o=m.loadOntologyFromOntologyDocument(new File("/home/zhangda/Desktop/Protege_5.0_beta/university.owl"));
	  Reasoner hermit=new Reasoner(o);
	  System.out.println(hermit.isConsistent());
	
}
	
}
