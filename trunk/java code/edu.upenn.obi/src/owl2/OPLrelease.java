package owl2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/** 
 * 	Make release version of OPL ontology including inferred superclasses axioms, need to use merged owl file 
 *
 *  @author Jie Zheng
 */
public class OPLrelease {

	/**
	 * @param args
	 */
	public static void main(String[] args) {       
 		// today's Date
 		Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd");
        String today = ft.format(date); 		

		String path = "C:/Documents and Settings/Jie/My Documents/Ontology/opl/";
	    String saveFilename = path + "release/" + today + "/opl_inferred.owl";	
		String inferOntURIStr = "http://purl.obolibrary.org/obo/opl/opl_inferredSuperClasses.owl";
	    String saveInferFilename = path + "release/" + today + "/opl_inferredSuperClasses.owl";	

        String bfo = "http://www.ifomis.org/bfo/1.1";
        String metaData = "http://purl.obolibrary.org/obo/iao/dev/ontology-metadata.owl";
   	
        String reasonerName = "hermit";

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();	                        
        OWLOntology ont = OntologyManipulator.load(path + "ontology/opl.owl", manager);
     	OWLDataFactory df = manager.getOWLDataFactory();

	    // get all imported ontologies
      	Set<OWLOntology> importOnts = ont.getImports();

      	// remove imports statements from the loaded ontology
     	for(OWLOntology importOnt: importOnts) {    		
     		IRI importOntIRI = importOnt.getOntologyID().getOntologyIRI();
    		if (importOntIRI.equals(IRI.create(metaData)) || importOntIRI.equals(IRI.create(bfo))) { 
    			RemoveImport ri = new RemoveImport(ont, df.getOWLImportsDeclaration(importOntIRI));
    			manager.applyChange(ri);
    		}
     	}
     	
     	// merge the removed imported ontologies to the loaded one
     	for(OWLOntology importOnt: importOnts) {
     		IRI importOntIRI = importOnt.getOntologyID().getOntologyIRI();
       		if (importOntIRI.equals(IRI.create(metaData)) || importOntIRI.equals(IRI.create(bfo))) { 
    			// OntologyManipulator.printPrefixNSs(manager, importOnt);
    			ont = OntologyManipulator.mergeToTargetOnt(manager, ont, importOnt);
    		}
     	} 
    	
      	// generate the inferred hierarchy and clean the super classes
     	OWLReasoner reasoner = OWLReasonerRunner.runReasoner(manager, ont, reasonerName);
    	
		ont = OWLReasonerRunner.getCleanedOntologyWithInferredSuperClasses(manager, ont, inferOntURIStr, reasoner);
		OntologyManipulator.saveToFile(manager, ont, saveFilename);
		
		if (manager.contains(IRI.create(inferOntURIStr))) {
			OWLOntology inferOnt = manager.getOntology(IRI.create(inferOntURIStr));
			OntologyManipulator.saveToFile(manager, inferOnt, saveInferFilename);
		}
	}
}
