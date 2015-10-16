package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.Utils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;

import org.apache.commons.lang3.StringEscapeUtils;

import czsem.gate.utils.GateUtils;

@CreoleResource(name = "czsem NormalizeTokenForms", comment = "Adds clean_lemma, clean_lemma_noAccents and form_noAccents features to existing tokens.")
public class NormalizeTokenForms extends AbstractLanguageAnalyser {
	private static final long serialVersionUID = -4427651577074969377L;
	
	protected String annotationSetName = null;
	protected String tokenAnnotationTypeName = "Token";
	protected String formFeatureName = "form";
	protected String lemmaFeatureName = "lemma";
	protected String tagFeatureName = "tag";
	protected String negationRegexp = "^..........N....$";
	
	@Override
	public void execute() throws ExecutionException {
		
		Document doc = getDocument();
		AnnotationSet as = doc.getAnnotations(getAnnotationSetName());
		AnnotationSet tocs = as.get(getTokenAnnotationTypeName());
		
		for (Annotation token : tocs) { 
			FeatureMap fm = token.getFeatures();
			
			String clean_lemma = cleanTokenLemma(fm, token);
			
			if (clean_lemma != null) {
				String noAccentsLemma = GateUtils.removeDiacritics(clean_lemma);
				fm.put("clean_"+getLemmaFeatureName()+"_noAccents", noAccentsLemma);
				
				String tag = (String) fm.get(getTagFeatureName());
				if (tag != null) {
					if (tag.matches(getNegationRegexp())) {
						fm.put("neg_clean_"+getLemmaFeatureName(), 
								"NEG"+clean_lemma);
						fm.put("neg_clean_"+getLemmaFeatureName()+"_noAccents", 
								"NEG"+noAccentsLemma);
					} else { 
						fm.put("neg_clean_"+getLemmaFeatureName(), 
								clean_lemma);
						fm.put("neg_clean_"+getLemmaFeatureName()+"_noAccents", 
								noAccentsLemma);
					}
				}
			}
			
			String form = (String) fm.get(getFormFeatureName());
			if (form == null)
			{
				form = Utils.stringFor(doc, token);
			}
			fm.put(getFormFeatureName()+"_noAccents", GateUtils.removeDiacritics(form));			
		}
	}
	
	
	/**
	 * copied from Treex::Tool::Lexicon::CS::truncate_lemma, added 'něco-2' -> 'něco' replacement (last line)
	 */
	public static String truncateLemma(String origLemma) { 
		String treex_res = origLemma.replaceFirst("((?:(`|_;|_:|_,|_\\^|))+)(`|_;|_:|_,|_\\^).+$", "$1");
		return treex_res.replaceFirst("^(.+)-\\d+$", "$1");
	}
	
	protected String cleanTokenLemma(FeatureMap fm, Annotation a) {
		String lemma = (String) fm.get(getLemmaFeatureName());
		String form = (String) fm.get(getFormFeatureName());
		
		if (form == null)
			form = Utils.stringFor(getDocument(), a);

		if (lemma == null)
			lemma = form;


		if (lemma.startsWith("&"))
			lemma = StringEscapeUtils.unescapeXml(lemma);
		
		if (form.startsWith("&"))
			form = StringEscapeUtils.unescapeXml(form);

		/*
		int slash = lemma.indexOf('-', 1);
		int under = lemma.indexOf('_', 1);
		int slashForm = form.indexOf('-', 1);
		int underForm = form.indexOf('_', 1);

		int substr_end = lemma.length();

		if (slash > 0 && slashForm < 0)
			substr_end = slash;
		if (under > 0 && underForm < 0)
			substr_end = Math.min(substr_end, under);

		String clean_lemma = lemma; 
		
		if (! lemma.isEmpty()) 
		{
			char first_ch = lemma.charAt(0);
	
			if (Character.isUpperCase(form.charAt(0))) {
				first_ch = Character.toUpperCase(first_ch);
			}
	
			clean_lemma = first_ch + lemma.substring(1, substr_end);
		}
		*/
		
		String clean_lemma = truncateLemma(lemma);
		
		clean_lemma = clean_lemma.replace('|', '_');

		fm.put("clean_"+getLemmaFeatureName(), clean_lemma);
		
		return clean_lemma;
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


	public String getFormFeatureName() {
		return formFeatureName;
	}


	@RunTime
	@CreoleParameter(defaultValue="form")
	public void setFormFeatureName(String formFeatureName) {
		this.formFeatureName = formFeatureName;
	}


	public String getLemmaFeatureName() {
		return lemmaFeatureName;
	}


	@RunTime
	@CreoleParameter(defaultValue="lemma")
	public void setLemmaFeatureName(String lemmaFeatureName) {
		this.lemmaFeatureName = lemmaFeatureName;
	}


	public String getTagFeatureName() {
		return tagFeatureName;
	}


	@RunTime
	@CreoleParameter(defaultValue="tag")
	public void setTagFeatureName(String tagFeatureName) {
		this.tagFeatureName = tagFeatureName;
	}


	public String getNegationRegexp() {
		return negationRegexp;
	}


	@RunTime
	@CreoleParameter(defaultValue="^..........N....$")
	public void setNegationRegexp(String negationRegexp) {
		this.negationRegexp = negationRegexp;
	}

}
