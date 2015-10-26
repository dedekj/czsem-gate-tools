package czsem.gate.externalannotator;

import org.testng.annotations.Test;

import czsem.gate.externalannotator.SequenceAnnotator;

import static org.testng.AssertJUnit.*;

public class SequenceAnnotatorTest 
{
	@Test(expectedExceptions = StringIndexOutOfBoundsException.class)
	public void testNextTokenError()
	{
		SequenceAnnotator sa = new SequenceAnnotator("Hallo this\nstrange  world !", 0);
		sa.nextToken("Hallo");
		sa.nextToken("this");
//		sa.nextToken("strange");
		sa.nextToken("great");
		sa.nextToken("world");
		sa.nextToken("!");
	}

	@Test
	public void testNextToken1()
	{
		SequenceAnnotator sa = new SequenceAnnotator("Hallo this\nstrange  world !", 0);
		sa.nextToken("Hallo");
		assertEquals(sa.lastStart(), 0);
		assertEquals(sa.lastEnd(), 5);
		
		sa.nextToken("this");
		assertEquals(sa.lastStart(), 6);
		assertEquals(sa.lastEnd(), 10);

		sa.nextToken("strange");
		assertEquals(sa.lastStart(), 11);
		assertEquals(sa.lastEnd(), 18);

		sa.nextToken("world");
		assertEquals(sa.lastStart(), 20);
		assertEquals(sa.lastEnd(), 25);

		 
		sa.backup();
		try
		{
			sa.nextToken("?");
		}
		catch (StringIndexOutOfBoundsException e)
		{
			sa.restorePreviousAndBackupCurrent();
			sa.nextToken("!");
			assertEquals(sa.lastStart(), 26);
			assertEquals(sa.lastEnd(), 27);
			
			return;
		}
		fail("Expected StringIndexOutOfBoundsException");

	}
	
	@Test
	public void testNextToken2()
	{
		
		String s = "Hallo this strange  world!";
		SequenceAnnotator sa = new SequenceAnnotator(s , 0);
		sa.nextToken("Hallo this strange world !");
		assertEquals(sa.lastStart(), 0);
		assertEquals(sa.lastEnd(), s.length());		
	}

	@Test
	public void testNextToken3()
	{
		String s = " Hallo this\nstrange  world!";
		SequenceAnnotator sa = new SequenceAnnotator(s, 0);
		sa.nextToken("Hallo this strange world !");
		assertEquals(sa.lastStart(), 1);
		assertEquals(sa.lastEnd(), s.length());		
	}

	@Test
	public void testNextToken4()
	{
		String s = 	"The BBC's Bethany Bell in Jerusalem says many people face shortages of food, medicine and fuel. Chancellor Alistair Darling says the new longer-term agreement will guarantee earnings growth for 5.5 million workers and will allow\n"+
					"departments to plan more effectively. However, the TUC says the government's pay target of 2% has put it on \"a collision course with six million public servants\". TUC general secretary Brendan Barber said long term pay deals could be agreed but only on certain terms. \"The problem is last year we saw the government impose pay deals of only around 2%. Inflation was running at over 4%, so millions of public service workers saw themselves facing a real cut in their living standards,\" he said. Police in England, Wales and Northern Ireland are in dispute with the government over the staging of the 2.5% pay rise - in Scotland it was paid in full. Thousands of prison staff in England and Wales also walked out in August over the government's decision to give them their pay rise in two stages. Civil servants have also staged industrial action, which could be repeated, while strikes have been threatened in the NHS and local government. Meanwhile, Mr. Brown has urged MPs to limit their own salary rises to below 2% keep them in line with those of public sector workers.";
		SequenceAnnotator sa = new SequenceAnnotator(s, 0);
		sa.nextToken("The BBC's Bethany Bell in Jerusalem says many people face shortages of food, medicine and fuel.");
		assertEquals(sa.lastStart(), 0);
		assertEquals(sa.lastEnd(), 95);		
		sa.nextToken("Chancellor Alistair Darling says the new longer-term agreement will guarantee earnings growth for 5.5 million workers and will allow departments to plan more effectively.");
		assertEquals(sa.lastStart(), 96);
		assertEquals(sa.lastEnd(), 266);		
					//However, the TUC says the government's pay target of 2% has put it on "a collision course with six million public servants".
		sa.nextToken("However, the TUC says the government's pay target of 2% has put it on`` a collision course with six million public servants''.");
		assertEquals(sa.lastStart(), 267);
		assertEquals(sa.lastEnd(), 391);
		sa.nextToken("``The problem is last year we saw the government impose pay deals of only around 2%.");
		assertEquals(sa.lastStart(), 497);
		assertEquals(sa.lastEnd(), 580);
		
	}

	@Test
	public void testNextToken5()
	{
		String s = 	"However, the TUC says the government's pay target of 2% has put it on \"a collision course with six million public servants\".";
		SequenceAnnotator sa = new SequenceAnnotator(s, 0);
		
		String [] tokens = {
				"However",
				",",
				"the",
				"TUC",
				"says",
				"the",
				"government",
				"'s",
				"pay",
				"target",
				"of",
				"2",
				"%",
				"has",
				"put",
				"it",
				"on``",
				"a",
				"collision",
				"course",
				"with",
				"six",
				"million",
				"public",
				"servants",
				".",
		};
				
		for (int i = 0; i < tokens.length; i++) {
			sa.nextToken(tokens[i]);
		}		
		assertEquals(sa.lastStart(), 123);
		assertEquals(sa.lastEnd(), 124);
	}
	
	@Test
	public void testNextToken6()
	{
		String s = 	"Honeywell said HIS 's Federal Systems Division is now a";
		SequenceAnnotator sa = new SequenceAnnotator(s, 0);
		sa.nextToken("Honeywell");
		assertEquals(0, sa.lastStart());
		sa.nextToken("said");
		assertEquals(10, sa.lastStart());
		sa.nextToken("HIS`");
		assertEquals(15, sa.lastStart());
		sa.nextToken("s");
		assertEquals(20, sa.lastStart());
		sa.nextToken("Federal");
		assertEquals(22, sa.lastStart());
	}
	
	@Test
	public void testNextToken7()
	{
		String s = 	"\n    The partnership has earlier offered 100 dlrs per share for"+
					"\nGenCorp -- a tire, broadcasting, plastics and aerospace"+
					"\nconglommerate.";
		SequenceAnnotator sa = new SequenceAnnotator(s, 0);
		sa.nextToken("The partnership has earlier offered 100 dlrs per share for GenCorp- a tire, broadcasting, plastics and aerospace conglommerate.");
		assertEquals(5, sa.lastStart());
		assertEquals(134, sa.lastEnd());
	}

	@Test
	public void testNextToken8()
	{
		String s = 	
			"\n    It added that it has also considered--but not yet"+
			"\ndecided--to buy additional Atcor shares, either in the open"+
			"\nmarket, in private transactions, through a tender offer or"+
			"\notherwise.";
			
		SequenceAnnotator sa = new SequenceAnnotator(s, 0);
		sa.nextToken("It added that it has also considered- but not yet decided- to buy additional Atcor shares, either in the open market, in private transactions, through a tender offer or otherwise.");
		assertEquals(5, sa.lastStart());
		assertEquals(184, sa.lastEnd());
	}


	@Test
	public void testNextTokenAngleBrackets()
	{
		String s = 	
 "FIRST WISCONSIN <FWB > TO BUY MINNESOTA BANK"
+"    MILWAUKEE, Wis., March 26 - First Wisconsin Corp said it"
+"plans to acquire Shelard Bancshares Inc for about 25 mln dlrs"
+"in cash, its first acquisition of a Minnesota -based bank ."
+"    First Wisconsin said Shelard is the holding company for two"
+"banks with total assets of 168 mln dlrs."
+"    First Wisconsin , which had assets at yearend of 7.1 billion"
+"dlrs, said the Shelard purchase price is about 12 times the"
+"1986 earnings of the bank."
+"    It said the two Shelard banks have a total of five offices"
+"in the Minneapolis-St. Paul area."
+" Reuter";

		SequenceAnnotator sa = new SequenceAnnotator(s, 0);
		sa.nextToken("FIRST WISCONSIN TO BUY MINNESOTA BANK MILWAUKEE, Wis., March 26- First Wisconsin Corp said it plans to acquire Shelard Bancshares Inc for about 25 mln dlrs in cash, its first acquisition of a Minnesota -based bank.");
		assertEquals(0, sa.lastStart());
		sa.nextToken("First Wisconsin said Shelard is the holding company for two banks with total assets of 168 mln dlrs.");
		assertEquals(228, sa.lastStart());
		sa.nextToken("First Wisconsin, which had assets at yearend of 7.1 billion dlrs, said the Shelard purchase price is about 12 times the 1986 earnings of the bank.");
		assertEquals(331, sa.lastStart());

		SequenceAnnotator sa2 = new SequenceAnnotator(s, 0);
		String sentStr2 = "FIRST WISCONSIN <FWB > TO BUY MINNESOTA BANK";
		sa2.nextToken(sentStr2);
		assertEquals(0, sa2.lastStart());
		assertEquals(sentStr2.length(), sa2.lastEnd());
	}


	@Test
	public void testNextTokenAngleBrackets2()
	{
		String s = "FIRST WISCONSIN <FWB > TO BUY MINNESOTA BANK";

		SequenceAnnotator sa = new SequenceAnnotator(s, 0);
		sa.nextToken("FIRST");
		assertEquals(0, sa.lastStart());
		sa.nextToken("WISCONSIN");
		assertEquals(6, sa.lastStart());
		assertEquals(15, sa.lastEnd());
		sa.nextToken("TO");
		assertEquals(23, sa.lastStart());
		assertEquals(25, sa.lastEnd());
		sa.nextToken("BUY");
		assertEquals(26, sa.lastStart());		
	}

	@Test
	public void testNextTokenAngleBrackets3()
	{
		String s = "FIRST WISCONSIN <FWB > TO BUY MINNESOTA BANK";

		SequenceAnnotator sa = new SequenceAnnotator(s, 0);
		sa.nextToken("FIRST");
		assertEquals(0, sa.lastStart());
		sa.nextToken("WISCONSIN");
		assertEquals(6, sa.lastStart());
		assertEquals(15, sa.lastEnd());
		sa.nextToken("<FWB >");
		assertEquals(16, sa.lastStart());
		assertEquals(22, sa.lastEnd());
		sa.nextToken("TO");
		assertEquals(23, sa.lastStart());
		assertEquals(25, sa.lastEnd());
		sa.nextToken("BUY");
		assertEquals(26, sa.lastStart());		
	}

	@Test
	public void testNextTokenAngleBrackets4()
	{
		String s = "FIRST WISCONSIN <FWB > TO BUY MINNESOTA BANK";

		SequenceAnnotator sa = new SequenceAnnotator(s, 0);
		sa.nextToken("FIRST");
		assertEquals(0, sa.lastStart());
		sa.nextToken("WISCONSIN");
		assertEquals(6, sa.lastStart());
		assertEquals(15, sa.lastEnd());
		sa.nextToken("<");
		assertEquals(16, sa.lastStart());
		assertEquals(17, sa.lastEnd());
		sa.nextToken("FWB");
		assertEquals(17, sa.lastStart());
		assertEquals(20, sa.lastEnd());
		sa.nextToken(">");
		assertEquals(21, sa.lastStart());
		assertEquals(22, sa.lastEnd());
		sa.nextToken("TO");
		assertEquals(23, sa.lastStart());
		assertEquals(25, sa.lastEnd());
		sa.nextToken("BUY");
		assertEquals(26, sa.lastStart());		
	}
	
	@Test
	public void testNextTokenZakonDot()
	{
		String s = 
		
		"\n   11d)  Zákon  č.  218/2000  Sb.,  o  rozpočtových  pravidlech  a o změně"
		+"\n   některých   souvisejících   zákonů   (rozpočtová  pravidla),  ve  znění"
		+"\n   pozdějších předpisů."
		+"\n"
		+"\n   Zákon  č. 250/2000 Sb., o rozpočtových pravidlech územních rozpočtů, ve"
		+"\n   znění pozdějších předpisů."
		+"\n"
		+"\n   11e) Nařízení (ES) č. 1606/2002 Evropského parlamentu a Rady ze dne 19."
		+"\n   července 2002, o používání Mezinárodních účetních standardů."
		+"\n";
		

		SequenceAnnotator sa = new SequenceAnnotator(s, 0);
		sa.nextToken("11d) Zákon č. 218/2000 Sb., o rozpočtových pravidlech a o změně některých souvisejících zákonů (rozpočtová pravidla), ve znění pozdějších předpisů.");
		assertEquals(4, sa.lastStart());
		sa.nextToken("Zákon č. 250/2000 Sb<<<DOT>>, o rozpočtových pravidlech územních rozpočtů, ve znění pozdějších předpisů. 11e) Nařízení (ES) č. 1606/2002 Evropského parlamentu a Rady ze dne 19. července 2002, o používání Mezinárodních účetních standardů.");
		assertEquals(179, sa.lastStart());
	}

	@Test
	public void testNextTokenDot() {
		SequenceAnnotator sa = new SequenceAnnotator(". konec", 0);
		sa.nextToken("DOT");
		sa.nextToken("konec");
		assertEquals(2, sa.lastStart());
		assertEquals(7, sa.lastEnd());
	}

}
