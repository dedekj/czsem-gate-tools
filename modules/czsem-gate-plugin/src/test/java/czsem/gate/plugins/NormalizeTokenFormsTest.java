package czsem.gate.plugins;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NormalizeTokenFormsTest {

	@Test
	public void truncateLemma() {
		Assert.assertEquals(NormalizeTokenForms.truncateLemma(""), "");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma("jeden`1"), "jeden");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma("přijímat_:T"), "přijímat");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma("pronajímaný_^(*2t)"), "pronajímaný");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma("–"), "–");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma("12345"), "12345");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma(""), "");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma("-"), "-");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma("`"), "`");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma("nízko-1_^(níže,_než...)"), "nízko");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma("den_^(jednotka_času)"), "den");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma("že-1"), "že");
		Assert.assertEquals(NormalizeTokenForms.truncateLemma("-1"), "-1");
		
		//Assert.assertEquals(NormalizeTokenForms.truncateLemma("U-50488"), "U-50488");
	}
}
