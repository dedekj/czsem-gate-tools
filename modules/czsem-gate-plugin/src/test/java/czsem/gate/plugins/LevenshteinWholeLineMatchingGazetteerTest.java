package czsem.gate.plugins;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LevenshteinWholeLineMatchingGazetteerTest {

	@Test
	public static void countDistanceOptimized() {
		LevenshteinWholeLineMatchingGazetteer g = new LevenshteinWholeLineMatchingGazetteer();
		
		Assert.assertEquals(g.countDistanceOptimized("aaa", "aaa", 0).diatnce, 0);
		Assert.assertEquals(g.countDistanceOptimized("aaa", "AAA", 0).diatnce, 3);
		g.setCaseSensitive(false);
		Assert.assertEquals(g.countDistanceOptimized("aaa", "AAA", 0).diatnce, 0);
		Assert.assertEquals(g.countDistanceOptimized("a a a", "AAA", 0).diatnce, 2);
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "AAA", 0).diatnce, 2);
		g.setRemoveRedundantSpaces(false);
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "AAA", 0).diatnce, 4);
		g.setRemoveAllSpaces(true);
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "AAA", 0).diatnce, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a  b", "AAA", 0).diatnce, 1);
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "A\tA\t\tA", 0).diatnce, 0);

		
		//Non-breaking_space
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "A aA", 0).diatnce, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a\u00a0  a", "A\tA\t\tA", 0).diatnce, 0);
		g.setRemoveAllSpaces(false);
		g.setRemoveRedundantSpaces(true);
		Assert.assertEquals(g.countDistanceOptimized("a  a", "A\tA", 0).diatnce, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a", "A A", 0).diatnce, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a", "A \tA", 0).diatnce, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a", "A \t            A", 0).diatnce, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "A a A", 0).diatnce, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a\u00a0  a", "A\tA\t\tA", 0).diatnce, 0);

		
		Assert.assertTrue(
				g.countDistanceOptimized(
						"                                                            Souhrn údajů o přípravku",
						"4.1. Terapeutické indikace", 0.0).diatnce > 0);
		Assert.assertTrue(
				g.countDistanceOptimized(
						"                                                            Souhrn údajů o přípravku",
						"4.1. Terapeutické indikace", 1.0).diatnce > 10);
	}
}
