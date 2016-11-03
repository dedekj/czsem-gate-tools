package czsem.fs.query.restrictions;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ReferencingRestrictionTest {
	
	public static void checkRefString(String ferStr, String name, String attr) {  
		String[] ret = ReferencingRestriction.tryParseRefString(ferStr);
		Assert.assertEquals(ret, new String [] {name, attr});
	}

	@Test
	public void tryParseRefString() {
		checkRefString("{N.a}", "N", "a");
		checkRefString("   {N.a}   ", "N", "a");
		checkRefString("{N.a.b.c.}", "N", "a.b.c.");
		checkRefString("{{N}.a}b}c}}", "{N}", "a}b}c}");
		
		Assert.assertNull(ReferencingRestriction.tryParseRefString("abc"));
		Assert.assertNull(ReferencingRestriction.tryParseRefString("abc.efg"));
		Assert.assertNull(ReferencingRestriction.tryParseRefString("{abcefg}"));
		Assert.assertNull(ReferencingRestriction.tryParseRefString("{abc.efg"));
		Assert.assertNull(ReferencingRestriction.tryParseRefString("abc.efg}"));
	}
}
