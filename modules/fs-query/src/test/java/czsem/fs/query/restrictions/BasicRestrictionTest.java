package czsem.fs.query.restrictions;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BasicRestrictionTest {

	@Test
	public void evaluate() {
		Assert.assertTrue(BasicRestriction.BR_EQ.evaluate(null, null));
		Assert.assertTrue(BasicRestriction.BR_EQ.evaluate(1, 1));
		Assert.assertTrue(BasicRestriction.BR_EQ.evaluate("a", "a"));

		Assert.assertFalse(BasicRestriction.BR_EQ.evaluate(null, 1));
		Assert.assertFalse(BasicRestriction.BR_EQ.evaluate(1, 2));
		Assert.assertFalse(BasicRestriction.BR_EQ.evaluate("a", "b"));
		Assert.assertTrue(BasicRestriction.BR_EQ.evaluate("1", 1));
		Assert.assertTrue(BasicRestriction.BR_EQ.evaluate(1, "1"));

		Assert.assertFalse(BasicRestriction.BR_NEQ.evaluate(null, null));
		Assert.assertFalse(BasicRestriction.BR_NEQ.evaluate(1, 1));
		Assert.assertFalse(BasicRestriction.BR_NEQ.evaluate("a", "a"));

		Assert.assertTrue(BasicRestriction.BR_NEQ.evaluate(null, 1));
		Assert.assertTrue(BasicRestriction.BR_NEQ.evaluate(1, 2));
		Assert.assertTrue(BasicRestriction.BR_NEQ.evaluate("a", "b"));
		Assert.assertFalse(BasicRestriction.BR_NEQ.evaluate(1, "1"));

		Assert.assertTrue(BasicRestriction.BR_GTEQ.evaluate(null, null));
		Assert.assertFalse(BasicRestriction.BR_GTEQ.evaluate(1, null));
		Assert.assertFalse(BasicRestriction.BR_GTEQ.evaluate(null, 1));
		Assert.assertTrue(BasicRestriction.BR_GTEQ.evaluate(1, 1));
		Assert.assertTrue(BasicRestriction.BR_GTEQ.evaluate(2, 1));
		Assert.assertTrue(BasicRestriction.BR_GTEQ.evaluate("b", "a"));
		
		Assert.assertFalse(BasicRestriction.BR_GTEQ.evaluate(1, 2));
		Assert.assertFalse(BasicRestriction.BR_GTEQ.evaluate("a", "b"));
		
		Assert.assertTrue(BasicRestriction.BR_GT.evaluate(2, 1));
		Assert.assertTrue(BasicRestriction.BR_GT.evaluate("b", "a"));
		Assert.assertFalse(BasicRestriction.BR_GT.evaluate("a", "a"));
		Assert.assertFalse(BasicRestriction.BR_GT.evaluate("a", "b"));
		
		Assert.assertTrue(BasicRestriction.BR_LT.evaluate(1, 2));
		Assert.assertTrue(BasicRestriction.BR_LT.evaluate("a", "b"));
		Assert.assertFalse(BasicRestriction.BR_LT.evaluate("a", "a"));
		Assert.assertFalse(BasicRestriction.BR_LT.evaluate("b", "a"));

		Assert.assertTrue(BasicRestriction.BR_LTEQ.evaluate(1, 2));
		Assert.assertTrue(BasicRestriction.BR_LTEQ.evaluate("a", "b"));
		Assert.assertTrue(BasicRestriction.BR_LTEQ.evaluate("a", "a"));
		Assert.assertFalse(BasicRestriction.BR_LT.evaluate("b", "a"));

		Assert.assertTrue(BasicRestriction.BR_GT.evaluate("2", 1));
		Assert.assertTrue(BasicRestriction.BR_GT.evaluate("20", 2));
		Assert.assertTrue(BasicRestriction.BR_GT.evaluate("21", "200"));
		Assert.assertTrue(BasicRestriction.BR_GT.evaluate("200", 21));
		Assert.assertTrue(BasicRestriction.BR_GT.evaluate(200, "21"));
		
		Assert.assertTrue(BasicRestriction.BR_EQ.evaluate(1.0, 1));
		Assert.assertTrue(BasicRestriction.BR_EQ.evaluate("1.0", 1));
		Assert.assertFalse(BasicRestriction.BR_EQ.evaluate("1.0", "1"));
	}
	
}
