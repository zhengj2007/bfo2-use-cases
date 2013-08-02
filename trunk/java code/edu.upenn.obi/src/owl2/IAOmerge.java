package owl2;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.util.AutoIRIMapper;

/** 
 * 	Merge several OWL files that make up IAO
 * 		current only merge obsolete.owl to iao.owl
 *		remove comment sign should merge all OWL files that used by IAO to one OWL file
 *
 *  @author Jie Zheng
 */
public class IAOmerge {
	public static void main(String[] args) {
		String obsolete = "http://purl.obolibrary.org/obo/iao/dev/obsolete.owl";
        //String externalDerived = "http://purl.obolibrary.org/obo/obi/externalDerived.owl";
        //String externalByHand = "http://purl.obolibrary.org/obo/obi/external-byhand.owl";

 		String path = "C:/Documents and Settings/Jie/My Documents/Ontology/iao/releases/2012-01-05/merged/";
	    String saveFilename = path + "iao-all.owl";	

	    // Get hold of an ontology manager
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();	 
    	/*
        AutoIRIMapper mapper = new AutoIRIMapper(new File(path + "branches/"), false);
       	AutoIRIMapper mapper2 = new AutoIRIMapper(new File(path + "external/"), false);
       	AutoIRIMapper mapper3 = new AutoIRIMapper(new File(path + "external/iao/"), false);
       	manager.addIRIMapper(mapper);
    	manager.addIRIMapper(mapper2);
    	manager.addIRIMapper(mapper3);
        */
                         
    	// load obi ontology
        OWLOntology ont = OntologyManipulator.load(path + "iao-all_old.owl", manager);
        OWLOntology obsoleteOnt = OntologyManipulator.load(path + "obsolete.owl", manager);
    	//OntologyManipulator.printPrefixNSs(manager, ont);

    	//OWLDataFactory df = manager.getOWLDataFactory();

	    // get all imported ontologies
      	//Set<OWLOntology> importOnts = ont.getImports();

      	/* remove imports statements from the loaded ontology
     	for(OWLOntology importOnt: importOnts) {    		
			// OntologyManipulator.printPrefixNSs(manager, importOnt);
			IRI importOntIRI = importOnt.getOntologyID().getOntologyIRI();
    		if (importOntIRI.equals(IRI.create(external)) || importOntIRI.equals(IRI.create(externalByHand)) || importOntIRI.equals(IRI.create(externalDerived))) { 
    			RemoveImport ri = new RemoveImport(ont, df.getOWLImportsDeclaration(importOntIRI));
    			manager.applyChange(ri);
    		}
     	}
     	*/     	
     	/* merge the removed imported ontologies to the loaded one
     	for(OWLOntology importOnt: importOnts) {
     		IRI importOntIRI = importOnt.getOntologyID().getOntologyIRI();
    		if (importOntIRI.equals(IRI.create(external)) || importOntIRI.equals(IRI.create(externalByHand)) || importOntIRI.equals(IRI.create(externalDerived))) {
    			// OntologyManipulator.printPrefixNSs(manager, importOnt);
    			ont = OntologyManipulator.mergeToTargetOnt(manager, ont, importOnt);
    		}
     	} 
     	*/
     	ont = OntologyManipulator.mergeToTargetOnt(manager, ont, obsoleteOnt);
     	
     	OntologyManipulator.saveToFile(manager, ont, saveFilename);
	}
}

