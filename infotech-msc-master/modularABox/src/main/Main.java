package main;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityCollector;

import com.google.common.collect.Iterables;

import um.ece.abox.module.condition.ConditionChecker;
import um.ece.abox.module.condition.SyntacticConditionChecker;
import um.ece.abox.module.extractor.ABoxModuleExtractor;
import um.ece.abox.module.extractor.SHIQABoxModuleExtractor;
import um.ece.abox.module.extractor.SHIQPropertyModuleExtractor;
import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.helper.OntologyRBoxHelper;
import um.ece.abox.module.helper.memo.SHIQOntologyABoxHelper;
import um.ece.abox.module.helper.memo.SHIQOntologyRBoxHelper;
import um.ece.abox.module.model.ABoxModule;
import um.ece.abox.module.parser.concept.QueryConceptParser;
import um.ece.abox.module.parser.ontology.OntologyParser;
import um.ece.abox.module.parser.ontology.SHIQOntologyParser;
import um.ece.abox.module.util.Statistics;
import um.ece.abox.module.util.TBoxModuleExtractor;



public class Main {
	
	OWLOntology tbox;
	OWLOntology abox;
	//OWLOntologyManager manager;
	public OntologyABoxHelper ah;
	OntologyRBoxHelper rh;
	OntologyParser ontParser;
	QueryConceptParser queryParser;
	ConditionChecker condChecker;
	ABoxModuleExtractor a_extractor;
	TBoxModuleExtractor t_extractor;
	
	public void init(String terminologyFile, String aboxFile) {
		
		try {
			File tfile = new File(terminologyFile);
			File afile = new File(aboxFile);
			
			long time = System.currentTimeMillis();
			
			System.out.println("Loading Ontology (T & A)...");
			tbox = OWLManager.createOWLOntologyManager()
								.loadOntologyFromOntologyDocument(tfile);
			abox = OWLManager.createOWLOntologyManager()
								.loadOntologyFromOntologyDocument(afile);
			System.out.println("Time: " + (System.currentTimeMillis()-time));
			time = System.currentTimeMillis();
			this.t_extractor = new TBoxModuleExtractor(tbox);
			
			System.out.println("Initializing ABox Helper...");
			ah = new SHIQOntologyABoxHelper(abox);
			
			System.out.println("Initializing RBox Helper...");
			rh = new SHIQOntologyRBoxHelper(tbox);
			
			System.out.println("Time: " + (System.currentTimeMillis()-time));
			time = System.currentTimeMillis();
			
			System.out.println("Analyzing Ontology TBox Axioms...");
			ontParser = new SHIQOntologyParser(tbox);
			ontParser.parseOntology();
			
			System.out.println("Time: " + (System.currentTimeMillis()-time));
			time = System.currentTimeMillis();
			
			System.out.println("Initializing SHIQ ABox Module Extractor...");
			condChecker = new SyntacticConditionChecker(tbox, ah, rh);
			ontParser.accept(condChecker);
			queryParser = new QueryConceptParser();
			a_extractor = new SHIQABoxModuleExtractor(ah, condChecker, 
													new SHIQPropertyModuleExtractor(ah, rh));
			System.out.println("Initialization Done.");
			System.out.println("Time: " + (System.currentTimeMillis()-time));
			//time = System.currentTimeMillis();
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	public void preprocess(OWLClassExpression query) {
		
		if (query == null)
			return;
		
		this.queryParser.parseQuery(query);
		this.queryParser.accept(this.condChecker);
		
		// extract TBox module,
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		OWLEntityCollector sigcollector = new OWLEntityCollector(signature);
		query.accept(sigcollector);
		OWLOntology tboxModule = this.t_extractor.extractModuleAsOntology(signature);
		
		// and re-parse the ontology
		this.ontParser = new SHIQOntologyParser(tboxModule);
		this.ontParser.parseOntology();
		this.ontParser.accept(this.condChecker);
	}
	
	public ABoxModule extractModule(OWLClassExpression query, OWLNamedIndividual ind) {
		
		return this.a_extractor.getABoxModule(ind);
	}
	
	public Set<ABoxModule> modularize(OWLClassExpression query) {
		
		return this.a_extractor.ABoxModularize();
	}
	
	
	
	public static void main(String args[]) {
		
		Main m = new Main();
		m.init("/home/zhangda/workspace/univ-bench.owl", "/home/zhangda/workspace/university.owl");
		m.preprocess(null); // query concept
		
		//"../Ont3/data/biopax3.owl", "../Ont3/data/Arabidopsis thaliana.owl"
		// "../Ont3/data/univ.owl", "../Ont3/data/lubm4.owl"
	//	TestABoxModules(m.ah, m);
	}
	
	
	static void TestABoxModules(OntologyABoxHelper ah, Main lcm) {

	Set<ABoxModule> modules = new HashSet<ABoxModule>();
	
	System.err.println("#Individuals "+Iterables.size(ah.getNamedIndividuals()));
	
	long cur = System.currentTimeMillis();
//	for (OWLNamedIndividual ind : ah.getNamedIndividuals()) 
//		modules.add(lcm.extractModule(null, ind));
	modules.addAll(lcm.modularize(null));
	long delta = System.currentTimeMillis() - cur;
	
	System.out.println("Ave. Module Extraction Time : " + ((double) delta) / Iterables.size(ah.getNamedIndividuals()));
	System.out.println("Total #Modules " + modules.size());
	
	// Statistics 
	double[] sigSizes = new double[modules.size()];
	double[] modSizes = new double[modules.size()];
	int i = 0, max1 = -1, max2 = -1;
	ABoxModule maxMod1=null, maxMod2=null;
	System.out.println("Counting...");
	for (ABoxModule amod : modules) {
		
		sigSizes[i] = amod.getSignature().size();
		modSizes[i++] = amod.getAssertions().size();
	
		if (amod.getSignature().size() > max1) {
			max1 = amod.getSignature().size();
			maxMod1 = amod;
		}
		
		if (amod.getAssertions().size() > max2) {
			max2 = amod.getAssertions().size();
			maxMod2 = amod;
		}
	}
	System.out.println("Counting DONE.");
	
	// print out the module with Max Signature
	System.out.println("The real max module (*sig, ass) : " + 
	maxMod1.getSignature().size() + "\t" + maxMod1.getAssertions().size());
	System.out.println("The real max module (sig, *ass) : " + 
	maxMod2.getSignature().size() + "\t" + maxMod2.getAssertions().size());
	
	double avgSigSize = Statistics.average(sigSizes);
	double avgModSize = Statistics.average(modSizes);
	
	double maxSigSize = Statistics.max(sigSizes);
	double masModSize = Statistics.max(modSizes);
	
	double minSigSize = Statistics.min(sigSizes);
	double minModSize = Statistics.min(modSizes);
	
	System.out.println("avgSigSize " + avgSigSize);
	System.out.println("avgModSize " + avgModSize);
	System.out.println("maxSigSize " + maxSigSize);
	System.out.println("masModSize " + masModSize);
	System.out.println("minSigSize " + minSigSize);
	System.out.println("minModSize " + minModSize);
	
	
	
	// Module Distribution
	int[] range = {10, 50, 100, 200, 500, 1000, 2000, 5000, 10000};
	int[] dis = new int[range.length + 1];
	String[] rs = {"0-10",
					"11--50", 
					"51--100",
					"101--200",
					"201--500",
					"501--1000",
					"1001--2000",
					"2001--5000",
					"5001--10000",
					"> 10000" };
	
	//	int[] modSizes = {2,12,101, 102, 203, 234,56,767,1001,100000};
	for (double j : modSizes) {
		int k = findPos(range, j);
		dis[k]++;
	}
	
	for (int k=0; k<dis.length; k++) 
		System.out.println(rs[k] + "\t" + dis[k]);

	// Module Signature Distribution 
	int[] range1 = {1, 5, 10, 20, 50, 100, 200, 500, 1000};
	int[] dis1 = new int[range.length + 1];
	String[] rs1 = {"0-1",
					"2--5", 
					"6--10",
					"11--20",
					"21--50",
					"51--100",
					"101--200",
					"201--500",
					"501--1000",
					"> 1000" };
	
	//	int[] modSizes = {2,12,101, 102, 203, 234,56,767,1001,100000};
	for (double j : sigSizes) {
		int k = findPos(range1, j);
		dis1[k]++;
	}
	
	for (int k=0; k<dis.length; k++) 
		System.out.println(rs1[k] + "\t" + dis1[k]);
	


	}
	
	private static int findPos(int[] range, double j) {

		for (int i=0; i<range.length; i++) 
			if (range[i]>=j)
				return i;
	
		return range.length;
	}

	
}


