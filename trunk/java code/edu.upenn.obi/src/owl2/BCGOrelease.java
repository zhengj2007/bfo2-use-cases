package owl2;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/** 
 * 	Make release version of BCGO ontology including inferred superclasses axioms, need to use merged owl file 
 *
 *  @author Jie Zheng
 */
public class BCGOrelease {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
 		String path = "C:/Documents and Settings/Jie/My Documents/Ontology/bcgo/ontology/";
	    String bcgoFilename = path + "bcgo_merged.owl";	
	    String saveFilename = path + "bcgo_merged_inferred.owl";	
		String inferOntURIStr = "http://purl.obolibrary.org/obo/bcgo/bcgo_inferredSuperClasses.owl";
	    String saveInferFilename = path + "bcgo_inferredSuperClasses.owl";	
	    
        String reasonerName = "hermit";
        		
	    // Get hold of an ontology manager
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();	                          
        OWLOntology bcgoOnt = OntologyManipulator.load(bcgoFilename, manager);
    	
      	// generate the inferred hierarchy and clean the super classes
     	OWLReasoner reasoner = OWLReasonerRunner.runReasoner(manager, bcgoOnt, reasonerName);
    	
		bcgoOnt = OWLReasonerRunner.getCleanedOntologyWithInferredSuperClasses(manager, bcgoOnt, inferOntURIStr, reasoner);
		OntologyManipulator.saveToFile(manager, bcgoOnt, saveFilename);
		
		if (manager.contains(IRI.create(inferOntURIStr))) {
			OWLOntology inferOnt = manager.getOntology(IRI.create(inferOntURIStr));
			OntologyManipulator.saveToFile(manager, inferOnt, saveInferFilename);
		}
	}
}
