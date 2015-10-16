package czsem.gate.plugins;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AddTokenOrthographyTest {

	@Test
	public static void getOrthographyValue() {
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("dedek"), "lowercase");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("ded-ek"), "lowercase");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("děd-ek"), "lowercase");
		
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("Dedek"), "upperInitial");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("Ded-ek"), "upperInitial");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("Jañ"), "upperInitial");
		
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("DEDEK"), "allCaps");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("DED-EK"), "allCaps");
		
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("DedekJ"), "mixedCaps");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("Dedek-J"), "mixedCaps");
		
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("123"), null);
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("-123.5"), null);

		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("jan dedek"), "lowercase");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("jan!dedek"), "lowercase");
	}
}
