package converter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

/**
 * @author Allen Xiang
 * 
 */
public class DoConversion {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String settingFileName = args[0];

			JSONObject jsonSettings = new JSONObject(readFile(settingFileName));
			String inputURI = jsonSettings.getString("download_url");
			String outputFile = jsonSettings.getString("output_file");

			JSONArray arrayImportMapping = jsonSettings
					.getJSONArray("import_mapping");

			JSONArray arrayObjectPropertyMapping = jsonSettings
					.getJSONArray("object_property_mapping");

			OWLOntologyManager man = OWLManager.createOWLOntologyManager();

			final HashMap<IRI, IRI> ontMap = new HashMap<IRI, IRI>();
			for (int i = 0; i < arrayImportMapping.length(); i++) {
				ontMap.put(IRI.create(arrayImportMapping.getJSONObject(i)
						.getString("to")), IRI.create(arrayImportMapping
						.getJSONObject(i).getString("from")));
			}

			OWLOntologyIRIMapper mapper = new OWLOntologyIRIMapper() {
				public IRI getDocumentIRI(IRI ontologyIRI) {
					return ontMap.get(ontologyIRI);
				}
			};

			man.addIRIMapper(mapper);

			OWLOntology ont = man.loadOntology(IRI.create(inputURI));
			OWLDataFactory df = man.getOWLDataFactory();


            
            
            
			IRI occurrent = IRI.create("http://purl.obolibrary.org/obo/BFO_0000003");
			IRI continuant = IRI.create("http://purl.obolibrary.org/obo/BFO_0000002");

            
			for (int i = 0; i < arrayObjectPropertyMapping.length(); i++) {
				OWLObjectPropertyExpression toReplace = df.getOWLObjectProperty(IRI
						.create(arrayObjectPropertyMapping.getJSONObject(i)
								.getString("toReplace")));
				
				
				OWLObjectPropertyExpression replacementO = df.getOWLObjectProperty(IRI
						.create(arrayObjectPropertyMapping.getJSONObject(i)
								.getString("replacementO")));

				OWLObjectPropertyExpression replacementC = df.getOWLObjectProperty(IRI
						.create(arrayObjectPropertyMapping.getJSONObject(i)
								.getString("replacementC")));


				ArrayList<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
				for (OWLEquivalentClassesAxiom axiom : ont
						.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {

					if (axiom.getObjectPropertiesInSignature().contains(toReplace)) {
						Iterator<OWLClassExpression> clss = axiom
								.getClassExpressions().iterator();
						OWLClassExpression clsL = clss.next();
						OWLClassExpression clsR = clss.next();


						
						RemoveAxiom removeAxiom = new RemoveAxiom(ont,
								(OWLAxiom) axiom);
						changes.add(removeAxiom);
		
		            	OWLObjectPropertyExpression replacement = replacementC;
			            if (hasSuperClass(ont, occurrent, clsL)){
			            	replacement = replacementO;
						}

			            OWLEquivalentClassesAxiom equiClassAiom = df
								.getOWLEquivalentClassesAxiom(
										clsL,
										replaceObjectPropertyExpression(df,
												clsR, toReplace, replacement));
		
						AddAxiom addAx = new AddAxiom(ont, equiClassAiom);
						changes.add(addAx);
					}
				}
				
				
				for (OWLSubClassOfAxiom axiom : ont
						.getAxioms(AxiomType.SUBCLASS_OF)) {
					
					if (axiom.getObjectPropertiesInSignature().contains(toReplace)) {
						RemoveAxiom removeAxiom = new RemoveAxiom(ont,
								(OWLAxiom) axiom);
						changes.add(removeAxiom);

		            	OWLObjectPropertyExpression replacement = replacementC;
			            if (hasSuperClass(ont, occurrent, axiom.getSubClass())){
			            	replacement = replacementO;
						}
						
						OWLSubClassOfAxiom subClassAiom = df
								.getOWLSubClassOfAxiom(
										axiom.getSubClass(),
										replaceObjectPropertyExpression(df,
												axiom.getSuperClass(), toReplace, replacement));
	
						AddAxiom addAx = new AddAxiom(ont, subClassAiom);
						changes.add(addAx);
					}

				}
				man.applyChanges(changes);
				
			}


			
            man.saveOntology(ont, man.getOntologyFormat(ont), IRI.create("file://"+outputFile));
		
		
		} catch (OWLOntologyCreationException e) {
			System.out.println("Could not create ontology");
			e.printStackTrace();
		} catch (JSONException e) {
			System.out.println("Could not parse json file");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not read input file");
			e.printStackTrace();
		} catch (UnknownOWLOntologyException e) {
			System.out.println("UnknownOWLOntologyException");
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			System.out.println("OWLOntologyStorageException");
			e.printStackTrace();
		}
	}

	private static boolean hasSuperClass(OWLOntology ont, IRI sup, OWLClassExpression sub) {
		for (OWLClassExpression supTemp : sub.asOWLClass().getSuperClasses(ont)) {
			if(supTemp.asOWLClass().getIRI().equals(sup)) {
				return true;
			}
			else {
				return hasSuperClass(ont, sup, supTemp);
			}
		}
		
		return false;
		
	}
	
	private static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		reader.close();
		return stringBuilder.toString();
	}

	private static OWLClassExpression replaceObjectPropertyExpression(
			OWLDataFactory df, OWLClassExpression target,
			OWLObjectPropertyExpression toReplace,
			OWLObjectPropertyExpression replacement) {

		if (!target.getObjectPropertiesInSignature().contains(toReplace)) {
			return target;
		}

		switch (target.getClassExpressionType()) {
		case OWL_CLASS:
			return target;
		case OBJECT_COMPLEMENT_OF:
			return df.getOWLObjectComplementOf(replaceObjectPropertyExpression(
					df, target.getComplementNNF(), toReplace, replacement));
		case OBJECT_INTERSECTION_OF:
			OWLObjectIntersectionOf intersects = (OWLObjectIntersectionOf) target;
			HashSet<OWLClassExpression> replacedIntersects = new HashSet<OWLClassExpression>();

			for (OWLClassExpression cls : intersects.asConjunctSet()) {
				replacedIntersects.add(replaceObjectPropertyExpression(df, cls,
						toReplace, replacement));
			}

			return df.getOWLObjectIntersectionOf(replacedIntersects);

		case OBJECT_UNION_OF:
			OWLObjectUnionOf unions = (OWLObjectUnionOf) target;
			HashSet<OWLClassExpression> replacedUnions = new HashSet<OWLClassExpression>();

			for (OWLClassExpression cls : unions.asDisjunctSet()) {
				replacedUnions.add(replaceObjectPropertyExpression(df, cls,
						toReplace, replacement));
			}

			return df.getOWLObjectUnionOf(replacedUnions);

		case OBJECT_SOME_VALUES_FROM:
			OWLObjectSomeValuesFrom someRestrict = (OWLObjectSomeValuesFrom) target;
			OWLObjectPropertyExpression someRole = someRestrict.getProperty();
			if (someRole.equals(toReplace))
				someRole = replacement;
			OWLClassExpression someFiller = someRestrict.getFiller();

			return df.getOWLObjectSomeValuesFrom(
					someRole,
					replaceObjectPropertyExpression(df, someFiller, toReplace,
							replacement));

		case OBJECT_ALL_VALUES_FROM:
			OWLObjectAllValuesFrom allRestrict = (OWLObjectAllValuesFrom) target;
			OWLObjectPropertyExpression allRole = allRestrict.getProperty();
			if (allRole.equals(toReplace))
				allRole = replacement;
			OWLClassExpression allFiller = allRestrict.getFiller();

			return df.getOWLObjectAllValuesFrom(
					allRole,
					replaceObjectPropertyExpression(df, allFiller, toReplace,
							replacement));

		case OBJECT_MAX_CARDINALITY:
			OWLObjectMaxCardinality maxRestrict = (OWLObjectMaxCardinality) target;

			int maxCardinality = maxRestrict.getCardinality();
			OWLClassExpression maxFiller = maxRestrict.getFiller();
			OWLObjectPropertyExpression maxRole = maxRestrict.getProperty();
			if (maxRole.equals(toReplace))
				maxRole = replacement;

			OWLClassExpression replacedMaxFiller = replaceObjectPropertyExpression(
					df, maxFiller, toReplace, replacement);
			return df.getOWLObjectMaxCardinality(maxCardinality, maxRole,
					replacedMaxFiller);

		case OBJECT_MIN_CARDINALITY:
			OWLObjectMinCardinality minRestrict = (OWLObjectMinCardinality) target;

			int minCardinality = minRestrict.getCardinality();
			OWLClassExpression minFiller = minRestrict.getFiller();
			OWLObjectPropertyExpression minRole = minRestrict.getProperty();
			if (minRole.equals(toReplace))
				minRole = replacement;

			OWLClassExpression replacedMinFiller = replaceObjectPropertyExpression(
					df, minFiller, toReplace, replacement);
			return df.getOWLObjectMinCardinality(minCardinality, minRole,
					replacedMinFiller);

		default:
			return target;
		}
	}
}