package owl2;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.util.AutoIRIMapper;

/** 
 * 	Merge all OWL files used by BCGO ontology to one OWL file
 *
 *  @author Jie Zheng
 */
public class BCGOmerge {
	public static void main(String[] args) {
		System.out.println(java.lang.Runtime.getRuntime().maxMemory()); 
		
		String path = "C:/Documents and Settings/Jie/My Documents/Ontology/bcgo/ontology/";
	    String saveFilename = path + "bcgo_merged.owl";	

	    // Get hold of an ontology manager
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();	 

        AutoIRIMapper mapper = new AutoIRIMapper(new File(path + "external/"), false);
       	manager.addIRIMapper(mapper);
                         
    	// load bcgo ontology
        OWLOntology ont = OntologyManipulator.load(path + "bcgo.owl", manager);

        OWLDataFactory df = manager.getOWLDataFactory();
        
        // get all imported ontologies
      	Set<OWLOntology> importOnts = ont.getImports();
      	Set<OWLImportsDeclaration> importSts = ont.getImportsDeclarations();

      	// remove imports statements from the loaded ontology
     	for(OWLImportsDeclaration importSt: importSts) {    		
			IRI importOntIRI = importSt.getIRI();
			System.out.println(importOntIRI.toString());
     		RemoveImport ri = new RemoveImport(ont, df.getOWLImportsDeclaration(importOntIRI));
    		manager.applyChange(ri);
     	}
     	
     	// merge the removed imported ontologies to the loaded one
     	for(OWLOntology importOnt: importOnts) {
     		// IRI importOntIRI = importOnt.getOntologyID().getOntologyIRI();
     		// OntologyManipulator.printPrefixNSs(manager, importOnt);
    		ont = OntologyManipulator.mergeToTargetOnt(manager, ont, importOnt);
     	} 
     	
     	OntologyManipulator.saveToFile(manager, ont, saveFilename);
	}
}

