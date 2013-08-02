package owl2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

/** 
 * 	Extract information of assay and all its subclasses including:
 *		- IRI, label, term editor, definition, parent classes, and whether have IEDB label
 *		- axioms represented in Manchester Syntax with human readable labels and separate by containing specific relations or classes
 *			. has_speciifed_input
 *	 		. has_speciifed_output
 *			. achieves_planned_objective
 *			. has participant
 *			. has part
 *			. evaluant role
 *			. analyte role
 *
 *  @author Jie Zheng
 */
public class ExtractClassesInfo {	
	/**
	 * @param args
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		String path = "C:/JavaDev/ontology/";
		String ontoFilename = "obi_merged.owl";
		String topClassStr = "http://purl.obolibrary.org/obo/OBI_0000070"; // assay
		String outFilename = "assays.tab";

		// Get hold of an ontology manager
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();	
    	// Create factory to obtain a reference to a class
        OWLDataFactory df = manager.getOWLDataFactory();   	
   	
        ManchesterOWLSyntaxOWLObjectRendererImpl rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl();
 
		StringBuffer participantRel = new StringBuffer("http://purl.obolibrary.org/obo/BFO_0000057"); 	// device
		StringBuffer inputRel = new StringBuffer("http://purl.obolibrary.org/obo/OBI_0000293"); 		// input material
		StringBuffer outputRel = new StringBuffer("http://purl.obolibrary.org/obo/OBI_0000299"); 		// output material
		StringBuffer objectiveRel = new StringBuffer("http://purl.obolibrary.org/obo/OBI_0000417");		// assay objective
		StringBuffer partRel = new StringBuffer("http://purl.obolibrary.org/obo/BFO_0000051");			// main process of assay
		StringBuffer evaluantCls = new StringBuffer("http://purl.obolibrary.org/obo/OBI_0000067");		// evaluant role
		StringBuffer analyteCls = new StringBuffer("http://purl.obolibrary.org/obo/OBI_0000275");		// analyte role

    	OWLAnnotationProperty defProp = df.getOWLAnnotationProperty(IRI.create(Config.DEF_AnnotProp));
    	OWLAnnotationProperty editorProp = df.getOWLAnnotationProperty(IRI.create(Config.EDITOR_AnnotPorp));
    	OWLAnnotationProperty IEDBProp = df.getOWLAnnotationProperty(IRI.create(Config.IEDB_SYN_AnnotProp));

    	// load ontology
    	OWLOntology ont = OntologyManipulator.load(path + ontoFilename, manager);
        
		// get all subclass of Assay
		OWLClass cls = df.getOWLClass(IRI.create(topClassStr));
		HashMap<String,OWLClass> allSubClasses = new HashMap<String,OWLClass>();
		allSubClasses.put(topClassStr, cls);
		allSubClasses = getSubClasses (allSubClasses, cls, ont);			
		System.out.println("\ntotal subClasses number: " + allSubClasses.size());
		
		// write file
		File assayText = new File(path + outFilename);
        FileOutputStream is = new FileOutputStream(assayText);
        OutputStreamWriter osw = new OutputStreamWriter(is);    
        BufferedWriter out = new BufferedWriter(osw);
        out.write("IRI\tLabel\tdefinition\teditor\tIEDB\tParent Classes\tEvquivalent Classes\tObjective\tInputs\tOutputs\tDevices\tMain Processes\tEvaluant\tAnalyte\n");
		
		Iterator it = allSubClasses.entrySet().iterator();		 
		while (it.hasNext()) {
			Map.Entry<String,OWLClass> entry = (Map.Entry<String,OWLClass>) it.next();
			String IRIstr = entry.getKey();
			cls = entry.getValue();
			String label = OBOentity.getLabel(cls, ont, df);
       		String definition = OBOentity.getStringAnnotProps (cls, df, ont, defProp);
       		definition = definition.replaceAll("\n", " ");
       		String editor = OBOentity.getStringAnnotProps (cls, df, ont, editorProp);
       		String iedb = "No";
       		if (OBOentity.getStringAnnotProps (cls, df, ont, IEDBProp).length() > 0) {
       			iedb = "Yes";
       		}			
			
			StringBuffer parentCls = new StringBuffer();
			StringBuffer equivalentAxiom = new StringBuffer();
			StringBuffer inputAxiom = new StringBuffer();
			StringBuffer outputAxiom = new StringBuffer();
			StringBuffer deviceAxiom = new StringBuffer();
			StringBuffer partAxiom = new StringBuffer();
			StringBuffer objectiveAxiom = new StringBuffer();
			StringBuffer evaluantAxiom = new StringBuffer();
			StringBuffer analyteAxiom = new StringBuffer();

			/*
			 * EquivalentClasses
			 */
			Set<OWLClassExpression> eqs = cls.getEquivalentClasses(ont);
	        for(OWLClassExpression exp : eqs) {
	        	String expStr = rendering.render(exp);
	        	expStr = convertToReadableRestriction (expStr, exp, ont, df);
	        	
	        	if (equivalentAxiom.length() > 0) {
	        		equivalentAxiom.append(", ");
    			} 
	        	equivalentAxiom.append(expStr);
	        	
	        	// objective
	    		if (exp.toString().contains(objectiveRel)) {
	    			if (objectiveAxiom.length() > 0) {
	    				objectiveAxiom.append(", ");
	    			} 
	    			objectiveAxiom.append(expStr);
	    		}	        	
	        	// input
	    		if (exp.toString().contains(inputRel)) {
	    			if (inputAxiom.length() > 0) {
	    				inputAxiom.append(", ");
	    			} 
	    			inputAxiom.append(expStr);
	    		} 
	    		// output
	    		if (exp.toString().contains(outputRel)) {
	    			if (outputAxiom.length() > 0) {
	    				outputAxiom.append(", ");
	    			} 
	    			outputAxiom.append(expStr);
	    		}	    		
	    		// device
	    		if (exp.toString().contains(participantRel)) {
	    			if (deviceAxiom.length() > 0) {
	    				deviceAxiom.append(", ");
	    			} 
	    			deviceAxiom.append(expStr);
	    		}
	    		// main step
	    		if (exp.toString().contains(partRel)) {
	    			if (partAxiom.length() > 0) {
	    				partAxiom.append(", ");
	    			} 
	    			partAxiom.append(expStr);
	    		}	
	    		// evaluant
	    		if (exp.toString().contains(evaluantCls)) {
	    			if (evaluantAxiom.length() > 0) {
	    				evaluantAxiom.append(", ");
	    			} 
	    			evaluantAxiom.append(expStr);
	    		}	    		
	    		// analyte
	    		if (exp.toString().contains(analyteCls)) {
	    			if (analyteAxiom.length() > 0) {
	    				analyteAxiom.append(", ");
	    			} 
	    			analyteAxiom.append(expStr);
	    		}	    		
	        }
 
			/*
			 * SuperClasses
			 */
            
	        Set<OWLClassExpression> supers = cls.getSuperClasses(ont);
	        for(OWLClassExpression exp : supers) {
	        	ClassExpressionType expType = exp.getClassExpressionType();
	        	if (expType.equals(ClassExpressionType.OWL_CLASS)) {
	        		OWLClass pCls = exp.asOWLClass();
	        		
	        		if (parentCls.length() > 0) {
	        			parentCls.append(", ");
	        		}
	        		parentCls.append(OBOentity.getLabel(pCls, ont, df));	
	        	} else {
		        	String expStr = rendering.render(exp);
		        	expStr = convertToReadableRestriction (expStr, exp, ont, df);
		        	
		        	// objective
		    		if (exp.toString().contains(objectiveRel)) {
		    			if (objectiveAxiom.length() > 0) {
		    				objectiveAxiom.append(", ");
		    			} 
		    			objectiveAxiom.append(expStr);
		    		}	        	
		        	// input
		    		if (exp.toString().contains(inputRel)) {
		    			if (inputAxiom.length() > 0) {
		    				inputAxiom.append(", ");
		    			} 
		    			inputAxiom.append(expStr);
		    		} 
		    		// output
		    		if (exp.toString().contains(outputRel)) {
		    			if (outputAxiom.length() > 0) {
		    				outputAxiom.append(", ");
		    			} 
		    			outputAxiom.append(expStr);
		    		}	    		
		    		// device
		    		if (exp.toString().contains(participantRel)) {
		    			if (deviceAxiom.length() > 0) {
		    				deviceAxiom.append(", ");
		    			} 
		    			deviceAxiom.append(expStr);
		    		}
		    		// main step
		    		if (exp.toString().contains(partRel)) {
		    			if (partAxiom.length() > 0) {
		    				partAxiom.append(", ");
		    			} 
		    			partAxiom.append(expStr);
		    		}	
		    		// evaluant
		    		if (exp.toString().contains(evaluantCls)) {
		    			if (evaluantAxiom.length() > 0) {
		    				evaluantAxiom.append(", ");
		    			} 
		    			evaluantAxiom.append(expStr);
		    		}	    		
		    		// analyte
		    		if (exp.toString().contains(analyteCls)) {
		    			if (analyteAxiom.length() > 0) {
		    				analyteAxiom.append(", ");
		    			} 
		    			analyteAxiom.append(expStr);
		    		}	        		
	        	}
	        }

    		out.write(IRIstr + "\t");
    		out.write(label + "\t");	
    		out.write(definition + "\t");	         		
    		out.write(editor + "\t");
    		out.write(iedb + "\t");
    		out.write(parentCls + "\t");
    		out.write(equivalentAxiom + "\t");
    		out.write(objectiveAxiom + "\t");
    		out.write(inputAxiom + "\t");
    		out.write(outputAxiom + "\t");	
    		out.write(deviceAxiom + "\t");
    		out.write(partAxiom + "\t");
    		out.write(evaluantAxiom + "\t");
    		out.write(analyteAxiom + "\n"); 
       		
	        /*
    		System.out.println("IRI: " + IRIstr);
       		System.out.println("label: " + label);	
        	System.out.println("definition: " + definition);	         		
       		System.out.println("editor: " + editor);
       		System.out.println("IEDB assay: " + iedb);
       		System.out.println("Parent: " + parentCls);
       		System.out.println("Equivalent: " + equivalentAxiom);
       		System.out.println("objective: " + objectiveAxiom);
       		System.out.println("inputs: " + inputAxiom);
       		System.out.println("outputs: " + outputAxiom);	
       		System.out.println("devices: " + deviceAxiom);
       		System.out.println("main processes: " + partAxiom);
       		System.out.println("evaluant: " + evaluantAxiom);
       		System.out.println("analyte: " + analyteAxiom); 
       		System.out.println("______________");
       		*/
	        
	        
		}
        out.close();
	}
	
	public static HashMap<String,OWLClass> getSubClasses (HashMap<String,OWLClass> allSubs, OWLClass topCls, OWLOntology ont) {
        Set<OWLClassExpression> subs = topCls.getSubClasses(ont);
        for(OWLClassExpression subCls : subs) {
        	ClassExpressionType clsType = subCls.getClassExpressionType();
        	if (clsType.equals(ClassExpressionType.OWL_CLASS)) {
        		OWLClass cls = subCls.asOWLClass();
        		String IRIstr = cls.getIRI().toString();
        		allSubs.put(IRIstr, cls);
        		allSubs = getSubClasses (allSubs, cls, ont);
        	}
        }
		
		return allSubs;
	}
	
	public static String convertToReadableRestriction (String restrict, OWLClassExpression exp, OWLOntology ont, OWLDataFactory df) {
		String obo = "http://purl.obolibrary.org/obo/"; 
		
		Set<OWLClass> ents = exp.getClassesInSignature();
 	    for(OWLClass ent : ents) {
	    	String entLabel = OBOentity.getLabel(ent, ont, df);
	    	String entID = ent.getIRI().toString().substring(obo.length());
	    	if (entLabel.contains(" ")) {
	    		entLabel = "'" + entLabel + "'";
	    	}   
	    	restrict = restrict.replace(entID, entLabel);
	    }
 	    
		Set<OWLObjectProperty> oProps = exp.getObjectPropertiesInSignature();
 	    for(OWLObjectProperty oProp : oProps) {
	    	String oPropLabel = OBOentity.getLabel(oProp, ont, df);
	    	String oPropID = oProp.getIRI().toString().substring(obo.length());
	    	if (oPropLabel.contains(" ")) {
	    		oPropLabel = "'" + oPropLabel + "'";
	    	}   
	    	restrict = restrict.replace(oPropID, oPropLabel);
	    }
 	    
		Set<OWLDataProperty> dProps = exp.getDataPropertiesInSignature();
 	    for(OWLDataProperty dProp : dProps) {
	    	String dPropLabel = OBOentity.getLabel(dProp, ont, df);
	    	String dPropID = dProp.getIRI().toString().substring(obo.length());
	    	if (dPropLabel.contains(" ")) {
	    		dPropLabel = "'" + dPropLabel + "'";
	    	}   
	    	restrict = restrict.replace(dPropID, dPropLabel);
	    }	    
 	    
		restrict = restrict.replace('\n', ' ');
		restrict = restrict.replaceAll("\\s+", " ");
		
		return restrict;
	}
}
