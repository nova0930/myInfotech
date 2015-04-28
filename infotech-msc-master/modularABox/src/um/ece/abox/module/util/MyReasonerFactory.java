package um.ece.abox.module.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

//import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

//import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class MyReasonerFactory {
	// different types of reasoner can be used
	public static final String PELLET_REASONER = "PELLET_REASONER",
								HERMIT_REASONER = "HERMIT_REASONER",
								RACER_REASONER = "RACER_REASONER",
								FACT_REASONER = "FACT_REASONER"; 									

	// build in reasoners
	static Map<String, OWLReasonerFactory> Factories = new HashMap<String, OWLReasonerFactory>();
	static Map<String, OWLReasonerConfiguration> Configurations = new HashMap<String, OWLReasonerConfiguration>();
	static OWLReasonerFactory Factory ;
	static String FactoryName ;

	static {
		//Factories.put("PELLET_REASONER", PelletReasonerFactory.getInstance());
		Factories.put("HERMIT_REASONER", new ReasonerFactory());
		//Factories.put("FACT_REASONER", new FaCTPlusPlusReasonerFactory());

		//Configurations.put("PELLET_REASONER", new SimpleConfiguration());
		Configurations.put("HERMIT_REASONER", new SimpleConfiguration());
		//Configurations.put("FACT_REASONER", new SimpleConfiguration());

		// DEFAULT REASONER FACTORY
		Factory = Factories.get("HERMIT_REASONER");
		FactoryName = "HERMIT_REASONER";
	}

	/**
	 * Add new Reasoner
	 * @param name, name of the reasoner
	 * @param f, factory object used to generate reasoner
	 * @param c, configuration for that reasoner
	 */
	public static void RegistorReasoner(String name, 
												OWLReasonerFactory f, 
												OWLReasonerConfiguration c) {
		Factories.put(name, f);
		Configurations.put(name, c);
	}

	/**
	 * List supported build in reasoners
	 * @return
	 */
	public static Set<String> ListReasoners() {
		return Factories.keySet();
	}

	/**
	 * Choose reasoner for debugging tasks
	 * @param name
	 */
	public static void SetReasoner(String name) {

		FactoryName = name;
		Factory = Factories.get(name);
		if (Factory == null) {
			System.err.println("INVALID FACTORY NAME. DEFAULT FACTORY IS SET.");
			Factory = Factories.get("HERMIT_REASONER");
			FactoryName = "HERMIT_REASONER";
		}
	}
	
	/**
	 * Get the reasoner from the reasoner factory.
	 * Different configurations may be applied according to different types of factories.
	 * @param ont
	 * @return
	 */
	public static OWLReasoner getReasoner(OWLOntology ont) {

		OWLReasonerConfiguration c = Configurations.get(FactoryName);
		//return Factory.createNonBufferingReasoner(ont, c);
		return Factory.createReasoner(ont, c);
	}
	
	public static OWLReasoner getNonBufferingReasoner(OWLOntology ont) {

		OWLReasonerConfiguration c = Configurations.get(FactoryName);
		//return Factory.createNonBufferingReasoner(ont, c);
		return Factory.createNonBufferingReasoner(ont, c);
	}
	
	public static OWLReasoner getReasoner(String name, OWLOntology ont) {
		
		String prev = FactoryName;
		SetReasoner(name);
		OWLReasoner r = getReasoner(ont);
		SetReasoner(prev);
		return r;
	}
}
