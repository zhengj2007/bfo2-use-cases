package owl2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/** 
 * Extract entity and its parent(s), including information of IRI, label and type
 * 
 * @author Jie Zheng
 * @version 
 */
public class ExtractEntityInfo {

	public static void main(String[] args) throws IOException {		
		String path = "C:/Documents and Settings/Jie/My Documents/Ontology/obi/trunk/src/ontology/branches/";
		String ontoFilename = "external-byhand2.owl";
		// String ontoFilename = "external.owl";
		String outFilename = "externalTerms.tab";

		// Get hold of an ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();	
		// Create factory to obtain a reference to a class
		OWLDataFactory df = manager.getOWLDataFactory();

    	// load ontology
    	OWLOntology ont = OntologyManipulator.load(path + ontoFilename, manager);
    	
    	HashMap<String, TermObject> termlist = new HashMap<String, TermObject>();
    	
    	Set<OWLEntity> ents = ont.getSignature();
    	
    	for(OWLEntity ent: ents) {
    		String IRIstr = ent.getIRI().toString();
    		TermObject termO = new TermObject();
    		termO.setIriStr(IRIstr);
    		termO.setLabel(OBOentity.getLabel(ent, ont, df));

    		if (ent.isOWLClass()) {
    			termO.setType("Class");

    			Set<OWLClassExpression> superExps = ent.asOWLClass().getSuperClasses(ont);
    			for (OWLClassExpression oe : superExps) {
    				if (oe.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
    					String label = OBOentity.getLabel(oe.asOWLClass(), ont, df);    					
    					termO.addParent(oe.asOWLClass().getIRI().toString() + "\t" + label);
    				}
    			}
    		} 
    		else if (ent.isOWLNamedIndividual()) {
    			termO.setType("Individual");

    			Set<OWLClassExpression> superExps = ent.asOWLNamedIndividual().getTypes(ont);
    			for (OWLClassExpression oe : superExps) {
    				if (oe.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
    					String label = OBOentity.getLabel(oe.asOWLClass(), ont, df);    					
    					termO.addParent(oe.asOWLClass().getIRI().toString() + "\t" + label);
    				}
    			}		
    		} 
    		else if (ent.isOWLObjectProperty()) {
    			termO.setType("ObjectProperty");
    			
    			Set<OWLObjectPropertyExpression> superExps = ent.asOWLObjectProperty().getSuperProperties(ont);
    			for (OWLObjectPropertyExpression oe : superExps) {
					String label = OBOentity.getLabel(oe.asOWLObjectProperty(), ont, df);
					termO.addParent(oe.getNamedProperty().getIRI().toString() + "\t" + label);
    			}     		
    		} 
    		else if (ent.isOWLAnnotationProperty()) {
    			termO.setType("AnnotationProperty");
    		
    			Set<OWLAnnotationProperty> superExps = ent.asOWLAnnotationProperty().getSuperProperties(ont);
    			for (OWLAnnotationProperty oe : superExps) {
					String label = OBOentity.getLabel(oe, ont, df);
					termO.addParent(oe.getIRI().toString() + "\t" + label);
    			}    
    		} 
     		termlist.put(IRIstr, termO);
    	}
    	
		Iterator<Map.Entry<String,TermObject>> it = termlist.entrySet().iterator();		 
		
		while (it.hasNext()) {
			Map.Entry<String,TermObject> entry = (Map.Entry<String,TermObject>) it.next();
			String IRIstr = entry.getKey();
			TermObject tObject = entry.getValue();
			System.out.print(tObject.getType() + "\t" + IRIstr + "\t" + tObject.getLabel() + "\t");
			ArrayList<String> parents = tObject.getParents();
			if (parents.size() >= 1 ) {
				System.out.print(parents.get(0));
				if (parents.size() > 1 ) {
					System.out.print("\tterm is asserted under multiple parents, need to check");
				} 
			}
			System.out.print("\n");
		}	
	}
}
