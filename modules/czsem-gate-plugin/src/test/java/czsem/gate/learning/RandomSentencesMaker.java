package czsem.gate.learning;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import czsem.gate.plugins.CustomPR;

public class RandomSentencesMaker
{
	public static Random rand = new Random();
	static String [] subjects = {"John", "Marry", "Alice"};
	static String [] verbs = {"had", "made", "killed", "loved", "kicked", "knew"};
	static String [] attrs = {"nice", "horrible", "great", "miserable", "casual"};
	static String [] objects = {"dog", "car", "friend", "cat", "table", "chair"};

	public static String getRandomWordPlusEmpty(String [] words)
	{
		if (rand.nextBoolean())
		{
			return "";
		} else {
			return getRandomWord(words);
		}
	}

	public static String getRandomWord(String [] words)
	{
		return words[rand.nextInt(words.length)];
	}

	public static String getRandomPrefixPhrase(String prefix, String word1, String sep)
	{
		if (rand.nextBoolean())
		{
			return word1;		
		} else {
			return prefix + sep + word1;					
		}		
	}

	public static String getRandomConjunctSubjectPhrase(RandomSentencesMaker sm, String sep)
	{
		if (rand.nextBoolean())
		{
			return sm.selectNewSubject();		
		} else {
			return sm.selectNewSubject() + sep + sm.selectNewSubject();					
		}		
	}

	public static String getRandomConjunctPhrase(String word1, String word2, String sep)
	{
		if (rand.nextBoolean())
		{
			return word1;		
		} else {
			return word1 + sep + word2;					
		}
	}

	
	
	public Set<Integer> relevantSents;
	int currentSent = 0;
	int relevantSubjIndex = 0; 
	int maxSentrs;

	public RandomSentencesMaker(int sentsNum)
	{
		relevantSents = new HashSet<Integer>(sentsNum);
		maxSentrs = sentsNum;
	}

	public String selectNewSubject()
	{
		int i = rand.nextInt(subjects.length);
		
		if (i == relevantSubjIndex) relevantSents.add(currentSent);
		
		return subjects[i];						
	}

	protected String createRandomSentence()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(getRandomConjunctSubjectPhrase(this, " and "));
		sb.append(' ');
		sb.append(getRandomWord(verbs));
		sb.append(' ');
		sb.append(
				getRandomConjunctPhrase(
						"a " +getRandomPrefixPhrase(getRandomWord(attrs), getRandomWord(objects), " "),
						"a " +getRandomPrefixPhrase(getRandomWord(attrs), getRandomWord(objects), " "),
						" and "));
		sb.append(".\n");
		
		
		return sb.toString();		
	}

	public String createRandomSentences()
	{
		StringBuilder sb = new StringBuilder();
		
		for (currentSent = 0; currentSent < maxSentrs; currentSent++) {
			sb.append(createRandomSentence());
		}
					
		return sb.toString();
	}
	
	public static class MarkRelevantTokens implements CustomPR.AnalyzeDocDelegate
	{
		String outAnnotationSet = null; 
		
		public void setTraining(boolean trainig)
		{
			if (trainig)
				outAnnotationSet = null;
			else
				outAnnotationSet = "Key";			
		}
	
		@Override
		public void analyzeDoc(Document doc) {
			
			@SuppressWarnings("unchecked")
			Set<Integer> relevant = (Set<Integer>) doc.getFeatures().get("relevantSents");
			
			List<Annotation> sents = Utils.inDocumentOrder(doc.getAnnotations().get("Sentence"));
			for (int i = 0; i < sents.size(); i++) {
				if (relevant.contains(i))
				{
					Annotation s = sents.get(i);
					AnnotationSet tocs = doc.getAnnotations().get("Token").getContained(
							s.getStartNode().getOffset(), 
							s.getEndNode().getOffset());
					
					for (Annotation t : tocs) {
						String ts = (String) t.getFeatures().get("string");
						if (Arrays.asList(objects).contains(ts))
						{
							FeatureMap fm = Factory.newFeatureMap();
							
							//Important!!!
							//"class" and "Mention" bellow must match with xml configuration file!!!
							fm.put("class", "relevant"); 
							doc.getAnnotations(outAnnotationSet).add(t.getStartNode(), t.getEndNode(), "Mention", fm );
						}
					}
				}
			}						
//			System.err.println(doc.getName());
//			System.err.println(relevant);
		}
	}
}