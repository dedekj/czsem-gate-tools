package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Utils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@CreoleResource(name = "czsem AddTokenOrthography", comment = "Adds orthography feature to existing tokens.")
public class AddTokenOrthography  extends AbstractLanguageAnalyser {
	private static final long serialVersionUID = 4814877037296713655L;

	protected String annotationSetName = null;
	protected String tokenAnnotationTypeName = "Token";

	@Override
	public void execute() throws ExecutionException {
		Document doc = getDocument();
		AnnotationSet as = doc.getAnnotations(getAnnotationSetName());
		AnnotationSet tocs = as.get(getTokenAnnotationTypeName());

		try {

			for (Annotation t : tocs) {
				String content = Utils.stringFor(doc, t);
				String val = getOrthographyValue(content);
				if (val != null) t.getFeatures().put("orth", val);
			}

		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

	public static final Set<Integer> lowerCaseTypesSet = new HashSet<Integer>(Arrays.asList(new Integer [] {
			(int) Character.LOWERCASE_LETTER,
			(int) Character.DASH_PUNCTUATION,
			(int) Character.FORMAT,
			}));

	public static final Set<Integer> upperCaseTypesSet = new HashSet<Integer>(Arrays.asList(new Integer [] {
			(int) Character.UPPERCASE_LETTER,
			(int) Character.DASH_PUNCTUATION,
			(int) Character.FORMAT,
			}));

	public static final Set<Integer> mixedCaseTypesSet = new HashSet<Integer>(Arrays.asList(new Integer [] {
			(int) Character.LOWERCASE_LETTER,
			(int) Character.UPPERCASE_LETTER,
			(int) Character.DASH_PUNCTUATION,
			(int) Character.FORMAT,
			}));
	
	public static String getOrthographyValue(String content) {
		if (content == null || content.isEmpty()) return null;
		
		Set<Integer> types = new HashSet<Integer>();
		
		for(int i = 1; i < content.length() ; i++) { 
		    char c = content.charAt(i);
		    types.add(Character.getType(c));
		}
		
		//we are ignoring spaces 
		types.remove(Character.getType(' '));

		//we are ignoring CONTROL chars 
		types.remove(Character.getType(':'));
		
		if (Character.getType(content.charAt(0)) == Character.UPPERCASE_LETTER)
		{
			if (lowerCaseTypesSet.containsAll(types))
				return "upperInitial";
		}

		types.add(Character.getType(content.charAt(0)));
		
		if (upperCaseTypesSet.containsAll(types))
			return "allCaps";
			
		if (lowerCaseTypesSet.containsAll(types))
			return "lowercase";
		
		if (mixedCaseTypesSet.containsAll(types))
			return "mixedCaps";
		
		return null;
	}


	public String getAnnotationSetName() {
		return annotationSetName;
	}

	@RunTime
	@CreoleParameter(defaultValue="")
	public void setAnnotationSetName(String annotationSetName) {
		this.annotationSetName = annotationSetName;
	}

	public String getTokenAnnotationTypeName() {
		return tokenAnnotationTypeName;
	}

	@RunTime
	@CreoleParameter(defaultValue="Token")
	public void setTokenAnnotationTypeName(String tokenAnnotationTypeName) {
		this.tokenAnnotationTypeName = tokenAnnotationTypeName;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		printCatgories('a');
		printCatgories('b');
		printCatgories('c');
		printCatgories('d');
		printCatgories('š');
		printCatgories('ě');

		printCatgories('D');
		printCatgories('Š');

		printCatgories('1');
		printCatgories('9');

		printCatgories('!');
		printCatgories('?');

		printCatgories('+');
		printCatgories('-');

		printCatgories('_');

	}

	private static void printCatgories(char c) {
		System.err.print(c + " " );
		System.err.format("%8s\n", 
				Integer.toBinaryString(
						Character.getType(c)));
		
	}

}
