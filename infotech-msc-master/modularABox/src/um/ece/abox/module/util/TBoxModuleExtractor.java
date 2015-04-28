package um.ece.abox.module.util;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.modularity.OntologySegmenter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class TBoxModuleExtractor {
	
	OntologySegmenter segmenter;
	
	public TBoxModuleExtractor(OWLOntology ont) {
		
		this.segmenter = new SyntacticLocalityModuleExtractor(ont.getOWLOntologyManager(), ont, ModuleType.TOP);
	}
	
	public OWLOntology extractModuleAsOntology(OWLEntity signature) {
		
		Set<OWLEntity> s = new HashSet<OWLEntity>();
		s.add(signature);
		return this.extractModuleAsOntology(s);
	}
	
	public OWLOntology extractModuleAsOntology(Set<OWLEntity> signature) {
		
		Set<OWLAxiom> module = this.segmenter.extract(signature);
		OWLOntology moduleOnt = null;
		
		try {
			moduleOnt = OWLManager.createOWLOntologyManager().createOntology(module);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return moduleOnt;
		
	}
}
