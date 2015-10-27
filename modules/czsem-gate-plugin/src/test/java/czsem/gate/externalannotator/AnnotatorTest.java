package czsem.gate.externalannotator;

import gate.Annotation;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Utils;
import gate.util.InvalidOffsetException;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.gate.externalannotator.Annotator.AnnotationSource;
import czsem.gate.externalannotator.Annotator.Sentence;
import czsem.gate.externalannotator.Annotator.SeqAnnotable;
import czsem.gate.externalannotator.SequenceAnnotator.CannotAnnotateCharacterSequence;
import czsem.gate.utils.GateUtils;

public class AnnotatorTest {
	
	public static class SeqAnnotableTest implements SeqAnnotable {
		protected SeqAnnotableTest(String string) {
			this.string = string;
		}

		private String string;
		@Override
		public String getAnnotationType() {	return "testType";}

		@Override
		public FeatureMap getFeatures() {return Factory.newFeatureMap();}

		@Override
		public void setGateAnnId(Integer gate_annotation_id) {}

		@Override
		public String getString() {	return string;	}		
	};

	public static class SentenceTest extends SeqAnnotableTest implements Sentence {
		private List<SeqAnnotable> tokens;

		protected SentenceTest(String sentenceString, List<SeqAnnotable> tokens) {
			super(sentenceString);			
			this.tokens = tokens;
		}

		@Override
		public String getAnnotationType() {	return "testSentenceType"; }
		
		@Override
		public List<SeqAnnotable> getOrderedTokens() {
			return tokens;
		}

		@Override
		public void annotateSecondaryEntities(AnnotatorInterface annotator)
				throws InvalidOffsetException {}
	}

	public static class AnnotationSourceTest implements AnnotationSource {
		private Iterable<Sentence> sentences;

		protected AnnotationSourceTest(Iterable<Sentence> sentences) {
			this.sentences = sentences;
		}

		@Override
		public Iterable<Sentence> getOrderedSentences() {
			return sentences;
		}
		
	}

	@Test(expectedExceptions = CannotAnnotateCharacterSequence.class)
	public static void safeAnnotateIterableSeq() throws Exception {
		setLogLevelOff();
		
		String docStr = "aaa bbb";
		
		GateUtils.initGateKeepLog();
		
		
		Annotator a = new Annotator();
		
		SeqAnnotable[] saa = new SeqAnnotableTest [] {
				new SeqAnnotableTest("aa"),
				new SeqAnnotableTest("1"),
				new SeqAnnotableTest("bb"),
		};
		List<SeqAnnotable> sa = Arrays.asList(saa);
		
		Document doc = Factory.newDocument(docStr);
		a.setAS(doc.getAnnotations());
		a.setSeqAnot(new SequenceAnnotator(doc));
//		a.safeAnnotateIterableSeq(sa);
		a.annotateIterableSeq(sa);
		
	}

	@Test
	public static void annotateTokensWhenSentencesWrong() throws Exception {
		setLogLevelOff();
		
		String docStr = "aaa bbb aaa ddd aaa";
		
		GateUtils.initGateKeepLog();
		
		
		SeqAnnotable[] saa1 = new SeqAnnotableTest [] {
				new SeqAnnotableTest("aaa"),
				new SeqAnnotableTest("bbb"),
		};

		SeqAnnotable[] saa2 = new SeqAnnotableTest [] {
				new SeqAnnotableTest("aaa"),
				new SeqAnnotableTest("ddd"),
		};

		SeqAnnotable[] saa3 = new SeqAnnotableTest [] {
				new SeqAnnotableTest("aaa"),
		};
		
		Sentence[] ss = { 
				new SentenceTest("xxx", Arrays.asList(saa1)),
				new SentenceTest("yyy", Arrays.asList(saa2)),
				new SentenceTest("zzz", Arrays.asList(saa3)),
		};

		
		Document doc = Factory.newDocument(docStr);

		Annotator a = new Annotator();
		a.annotate(new AnnotationSourceTest(Arrays.asList(ss)), doc, null);
		
		List<Annotation> ordered = Utils.inDocumentOrder(doc.getAnnotations());
		
		int toc = 0;
		long offset= 3;
		
		Assert.assertEquals(ordered.get(toc).getType(), "testType");
		Assert.assertEquals((long) ordered.get(toc).getEndNode().getOffset(), offset);
		toc++; offset+= 4;
		Assert.assertEquals(ordered.get(toc).getType(), "testType");
		Assert.assertEquals((long) ordered.get(toc).getEndNode().getOffset(), offset);
		toc++; offset+= 4;
		Assert.assertEquals(ordered.get(toc).getType(), "testType");
		Assert.assertEquals((long) ordered.get(toc).getEndNode().getOffset(), offset);
		toc++; offset+= 4;
		Assert.assertEquals(ordered.get(toc).getType(), "testType");
		Assert.assertEquals((long) ordered.get(toc).getEndNode().getOffset(), offset);
		toc++; offset+= 4;
		Assert.assertEquals(ordered.get(toc).getType(), "testType");
		Assert.assertEquals((long) ordered.get(toc).getEndNode().getOffset(), offset);
		toc++; offset+= 4;
	}
	
	@Test
	public static void annotateSentencesWhenTokensWrong() throws Exception {
		setLogLevelOff();
		
		String docStr = "aaa bbb aaa bbb aaa";
		
		GateUtils.initGateKeepLog();
		
		
		SeqAnnotable[] saa1 = new SeqAnnotableTest [] {
				new SeqAnnotableTest("xxx"),
				new SeqAnnotableTest("yyy"),
		};

		SeqAnnotable[] saa2 = new SeqAnnotableTest [] {
				new SeqAnnotableTest("zzz"),
				new SeqAnnotableTest("yyy"),
		};

		SeqAnnotable[] saa3 = new SeqAnnotableTest [] {
				new SeqAnnotableTest("xxx"),
		};
		
		Sentence[] ss = { 
				new SentenceTest("aaa bbb", Arrays.asList(saa1)),
				new SentenceTest("aaa bbb", Arrays.asList(saa2)),
				new SentenceTest("aaa", Arrays.asList(saa3)),
		};

		
		Document doc = Factory.newDocument(docStr);

		Annotator a = new Annotator();
		a.annotate(new AnnotationSourceTest(Arrays.asList(ss)), doc, null);
		
		List<Annotation> ordered = Utils.inDocumentOrder(doc.getAnnotations());
		
		int toc = 0;
		
		Assert.assertEquals(ordered.get(toc).getType(), "testSentenceType");
		Assert.assertEquals((long) ordered.get(toc).getEndNode().getOffset(), 7);
		toc++;
		Assert.assertEquals(ordered.get(toc).getType(), "testSentenceType");
		Assert.assertEquals((long) ordered.get(toc).getEndNode().getOffset(), 15);
		toc++;
		Assert.assertEquals(ordered.get(toc).getType(), "testSentenceType");
		Assert.assertEquals((long) ordered.get(toc).getEndNode().getOffset(), 19);
	}

	@Test
	public static void safeAnnotateIterableSeqDot() throws Exception {
		setLogLevelErr();
		
		String docStr = ". konec";
		
		GateUtils.initGateKeepLog();
		
		
		Annotator a = new Annotator();
		
		SeqAnnotable[] saa = new SeqAnnotableTest [] {
				new SeqAnnotableTest("<"),
				new SeqAnnotableTest("<"),
				new SeqAnnotableTest("<"),
				new SeqAnnotableTest("DOT"),
				new SeqAnnotableTest(">"),
				new SeqAnnotableTest(">"),
				new SeqAnnotableTest("konec"),
		};
		List<SeqAnnotable> sa = Arrays.asList(saa);
		
		Document doc = Factory.newDocument(docStr);
		a.setAS(doc.getAnnotations());
		a.setSeqAnot(new SequenceAnnotator(doc));
//		a.safeAnnotateIterableSeq(sa);
		a.annotateIterableSeq(sa);
	}

	public static void setLogLevelErr() {
		Logger.getLogger(Annotator.class).setLevel(Level.ERROR);		
	}

	public static void setLogLevelOff() {
		Logger.getLogger(Annotator.class).setLevel(Level.OFF);		
	}
}
