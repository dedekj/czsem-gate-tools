package czsem.fs.query;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.fs.NodeAttributes;
import czsem.fs.TreeIndex;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.FSQuery.QueryObject;
import czsem.fs.query.FSQueryParser.SyntaxError;

public class FSQueryParserTest {
	
	@Test(expectedExceptions = SyntaxError.class)
	public static void testParseExcept() throws SyntaxError {
		FSQueryParser p = new FSQueryParser(new FSQueryBuilderImpl());
		
		p.parse("foo");
	}

	@Test
	public static void testParseName() throws SyntaxError {
		FSQueryBuilderImpl b = new FSQueryBuilderImpl();
		FSQueryParser p = new FSQueryParser(b);
		p.parse("[_name=node1]");
		
		Assert.assertEquals(b.getRootNode().getName(), "node1");
	}
	
	@Test
	public static void testParse() throws SyntaxError {
		//TODO
		//Utils.loggerSetup(Level.ALL);
		
		FSQueryParser p = new FSQueryParser(new FSQueryBuilderImpl());
		p.parse("[]");

		p = new FSQueryParser(new FSQueryBuilderImpl());				
		p.parse("[string=visualized,kind=word,dependencies=\\[nsubjpass(3)\\, aux(5)\\, auxpass(7)\\, prep(11)\\],length=10]([string=annotations]([],[]),[])");
		
		evalQuery("[]( [id=1] , [id=2] )", new int [] {0, 1, 2});		
		evalQuery("[]( [id=1] , [id =2] )", new int [] {0, 1, 2});		
		evalQuery("[]( [id=1] , [ id =2] )", new int [] {0, 1, 2});		
		
		//because id is Number:
		evalQuery("[]( [id=1] , [ id = 2] )", new int [] {0, 1, 2});		
		evalQuery("[]( [id=1] , [ id =2 ] )", new int [] {0, 1, 2});		
		evalQuery("[]( [id=1] , [ id = 2 ] )", new int [] {0, 1, 2});		
	}
	
	@Test
	public static void testParseAndEvaluate() throws SyntaxError {
		FSQueryBuilderImpl b = new FSQueryBuilderImpl();
		FSQueryParser p = new FSQueryParser(b);
		
		p.parse("[]([]([]))");
		
		FSQueryTest.evaluateQuery(b.getRootNode());
		
		b = new FSQueryBuilderImpl();
		p = new FSQueryParser(b);
		
		p.parse("[]([],[])");
		
		int[] res = {
				0, 1, 1,
				0, 1, 2,
				0, 1, 7,
				0, 2, 1,
				0, 2, 2,
				0, 2, 7,
				0, 7, 1,
				0, 7, 2,
				0, 7, 7,};
		FSQueryTest.evaluateQuery(b.getRootNode(), res);

		
		b = new FSQueryBuilderImpl();
		p = new FSQueryParser(b);
		
		p.parse("[]([],[id=7])");
		
		int[] res2 = {
				0, 1, 7,
				0, 2, 7,
				0, 7, 7,};
		
		FSQueryTest.evaluateQuery(b.getRootNode(), res2);

	}

	public static void evalQuery(QueryData data, String queryString, int[] res) throws SyntaxError {
		QueryObject qo = FSQuery.buildQuery(queryString);

		FSQueryTest.evaluateQuery(data, qo, res);				
	}


	public static void evalQuery(String queryString, int[] res) throws SyntaxError {
		QueryData data = FSQueryTest.buidQueryObject();		
		evalQuery(data, queryString, res);
	}
	
	@Test
	public static void testDeeperNesting() throws SyntaxError {

		evalQuery("[id=0]([]([id=6]))", new int [] {});
		evalQuery("[id=0]([]([id=4]))", new int [] {0, 1, 4});
		evalQuery("[id=0]([id=1]([]))", new int [] {0, 1, 3,
												0, 1, 4});
		evalQuery("[id=0]([]([]([id=6])))", new int [] {0, 1, 3, 6});
		evalQuery("[id=0]([]([]([id=x])))", new int [] {});
		evalQuery("[id=0]([]([]([]([]))))", new int [] {});

		evalQuery("[id=0]([]([]([])),[]([]([])))", new int [] {0, 1, 3, 6, 1, 3, 6});
		evalQuery("[id=0]([]([]([])),[]([id=4]([])))", new int [] {});
		evalQuery("[id=0]([]([]([])),[]([id=4]))", new int [] {0, 1, 3, 6, 1, 4});
	}

	@Test
	public static void testTwoBrothers() throws SyntaxError {
		evalQuery("[]([]([id=4],[id=3]))", new int [] {	0, 1, 4, 3});
		evalQuery("[]([]([id=3],[id=4]))", new int [] {	0, 1, 3, 4});
		evalQuery("[]([id=2])", new int [] {0, 2});
		evalQuery("[]([id=2]([id=5]))", new int [] {0, 2, 5});
		evalQuery("[]([]([id=5]))", new int [] {0, 2, 5});
	}

	@Test
	public static void testThreeBrothers1() throws SyntaxError {
		QueryData data = FSQueryTest.buidQueryObject();		
		TreeIndex i = data.getIndex();
		i.addDependency(1, 12);
		evalQuery(data, "[]([]([id=12],[id=3],[]))", new int [] {	
				0, 1, 12, 3, 3,
				0, 1, 12, 3, 4,
				0, 1, 12, 3, 12,
				});		

		evalQuery(data, "[]([]([id=12],[id=3],[id=4]))", new int [] {	
				0, 1, 12, 3, 4,
				});		
	}

	@Test
	public static void testThreeBrothers2() throws SyntaxError {
		QueryData data = FSQueryTest.buidQueryObject();		
		TreeIndex i = data.getIndex();
		i.addDependency(0, 8);
		i.addDependency(8, 9);
		i.addDependency(8, 10);
		i.addDependency(8, 11);
		
		evalQuery(data, "[id=0]([]([],[],[]))", new int [] {	0, 1, 3, 3, 3,
														0, 1, 3, 3, 4,
														0, 1, 3, 4, 3,
														0, 1, 3, 4, 4,
														0, 1, 4, 3, 3,
														0, 1, 4, 3, 4,
														0, 1, 4, 4, 3,
														0, 1, 4, 4, 4,
														0, 2, 5, 5, 5,
														0, 8, 9, 9, 9,
														0, 8, 9, 9, 10,
														0, 8, 9, 9, 11,
														0, 8, 9, 10, 9,
														0, 8, 9, 10, 10,
														0, 8, 9, 10, 11,
														0, 8, 9, 11, 9,
														0, 8, 9, 11, 10,
														0, 8, 9, 11, 11,
														0, 8, 10, 9, 9,
														0, 8, 10, 9, 10,
														0, 8, 10, 9, 11,
														0, 8, 10, 10, 9,
														0, 8, 10, 10, 10,
														0, 8, 10, 10, 11,
														0, 8, 10, 11, 9,
														0, 8, 10, 11, 10,
														0, 8, 10, 11, 11,
														0, 8, 11, 9, 9,
														0, 8, 11, 9, 10,
														0, 8, 11, 9, 11,
														0, 8, 11, 10, 9,
														0, 8, 11, 10, 10,
														0, 8, 11, 10, 11,
														0, 8, 11, 11, 9,
														0, 8, 11, 11, 10,
														0, 8, 11, 11, 11,
														});
		evalQuery(data, "[]([id=8]([id=9]))", new int [] {	0, 8, 9,});
		evalQuery(data, "[]([]([id=9]))", new int [] {	0, 8, 9,});
		evalQuery(data, "[]([]([id=11],[id=10],[id=9]))", new int [] {	0, 8, 11, 10, 9,});
	}

	@Test
	public static void testOptional() throws SyntaxError {
		FSQueryBuilderImpl b = new FSQueryBuilderImpl();
		FSQueryParser p = new FSQueryParser(b);
		p.parse("[id=1, _optional=true]");
		
		Assert.assertTrue(b.getRootNode().isOptional(), "Optionalnot detected");


		evalQuery("[id=0]([id=2,_optional=true])", new int [] {0, 2});
		evalQuery("[id=0]([id=1,_optional=true])", new int [] {0, 1});

		evalQuery("[id=0]([_optional=true,_name=opt]([id=7]))", new int [] {0, 7});

		evalQuery("[id=0]([_optional=true])", new int [] {
				0, 1,
				0, 2,
				0, 7,
				});

		evalQuery("[id=0]([_optional=true,_name=opt1]([id=xxx]))", new int [] {});
		
		//the right combination
		evalQuery("[id=0]([_optional=true,id=x]([_optional=true,id=1]([_optional=true,id=x]([_optional=true,id=3]([_optional=true,id=x]([_optional=true,id=6]))))))", 
				new int [] {0, 1, 3, 6});

		evalQuery("[id=0]([_optional=true,_name=opt1]([_optional=true,_name=opt2]([id=7])))", new int [] {0, 7});

		evalQuery("[id=0]([_optional=true,_name=opt1]([id~=\\[124\\]]))", new int [] {
				0, 1, 4,
				});
		
		evalQuery("[id=0]([_optional=true,_name=opt1]([_optional=true,_name=opt2]([id~=\\[165\\]])))", new int [] {
				0, 1, 3, 6,
				//0, 2, 5, //only the largest match is considered, "all possibilities" evaluation mode - TODO
				});
		
		evalQuery("[id=0]([_optional=true,_name=opt1]([_optional=true,_name=opt2]([id~=\\[345\\]])))", new int [] {
				0, 1, 3,
				0, 1, 4,
				0, 2, 5,
				//0, 2, 5,
				});
		
		evalQuery("[]([id=1]([_optional=true,_name=opt1]([_optional=true,_name=opt2]([id~=\\[346\\]]))))", new int [] {
				0, 1, 3, 6,
				});

		evalQuery("[id=0]([]([id=3]),[id=1,_optional=true]([]([id@=3;6])))", new int [] {
				0, 1, 3, 1, 3, 6,
				});

		evalQuery("[id=0]([]([id=3]),[id=x,_optional=true]([]([id@=3;6])))", new int [] {
				0, 1, 3, 1, 3,
				});

		//two possibilities
		evalQuery("[id=0]([]([_optional=true]([id@=5;6])))", new int [] {
				0, 1, 3, 6, 
				
				//this should match in "all possibilities" evaluation mode - TODO 
				//0, 2, 5, 				
				});

		
		QueryData data = FSQueryTest.buidQueryObject();
		data.getIndex().addDependency(4, 8);
		evalQuery(data, "[]([id=1]([_optional=true,_name=opt1]([_optional=true,_name=opt2]([id~=\\[3468\\]]))))", new int [] {
				0, 1, 3, 6,
				0, 1, 4, 8,
				});

	}

	@Test
	public static void testRegexp() throws SyntaxError {
		evalQuery("[]([id~=\\[12\\]])", new int [] {0, 1, 0, 2});
		
		
		
		TreeIndex index = new TreeIndex();		
		index.addDependency(0,1);
		index.addDependency(0,2);
		index.addDependency(0,3);
		index.addDependency(0,4);

		QueryData data = new FSQuery.QueryData(index, new NodeAttributes.IdNodeAttributes() {

			@Override
			public Object getValue(int node_id, String attrName) {
				switch (node_id) {
				case 0:					
					return "string0";
				case 1:					
					return "string1";
				case 2:					
					return "string2";
				case 3:					
					return "#PersPron";
				case 4:					
					return "a#string4";
				default:
					return "";
				}
			}
		});

		evalQuery(data, "[]([str~=\\[^#\\].*])", new int [] {0, 1, 0, 2, 0, 4});
		evalQuery(data, "[]([str~=\\[#\\].*])", new int [] {0, 3});

	}

	@Test
	public static void testNotEqual() throws SyntaxError {
		evalQuery("[id=0]([id!=1])", new int [] {0, 2, 0, 7});		
		evalQuery("[id=0]([ id !=1])", new int [] {0, 2, 0, 7});		
	}

	//TODO _subtree_eval_depth is not working
	/*
	@Test
	public static void testIterateSubtree() throws SyntaxError {
		evalQuery("[_name=root]([id=1,_name=one]([_subtree_eval_depth=20]))", new int [] {
				0, 1,
				0, 1, 4, 
				0, 1, 3, 
				0, 1, 3, 4, 
				0, 1, 3, 6, 
				0, 1, 3, 6, 4});		
	}

	@Test
	public static void testIterateSubtreeComplex() throws SyntaxError {
		//_subtree_eval_depth doesn't work with child nodes...
		evalQuery("[_name=root]([_name=subtree,_subtree_eval_depth=20]([id@=1;2;3;4;5;6;7;8]))", new int [] {
				0, 1, 3,  
				0, 1, 4,  
				0, 2, 5, 		
				});		
	}
	*/

	@Test
	public static void testInListOperator() throws SyntaxError {
		evalQuery("[_name=root]([id @=2;7])", new int [] {
				0, 2,
				0, 7, 
				});		
	}

	@Test
	public static void testSubclassOperator() throws SyntaxError {
		evalQuery("[_name=root]([id<<2])", new int [] {
				0, 1,
				0, 2,
				});		
	}

	@Test
	public static void testOrderOperator() throws SyntaxError {
		/*
		evalQuery("[]([],[])", new int [] {
				0, 1, 1,
				0, 1, 2,
				0, 1, 7,
				0, 2, 1,
				0, 2, 2,
				0, 2, 7,
				0, 7, 1,
				0, 7, 2,
				0, 7, 7,});
		*/

		evalQuery("[id=0]([],[id>5])", new int [] {
				0, 1, 7,
				0, 2, 7,
				0, 7, 7,});

	}

	@Test
	public static void testRefSetrAndOptional() throws SyntaxError {
		evalQuery("[id=0]([_name=a],[_optional=true]([id={a.id}]))", new int [] {
				0, 1, 1,
				0, 2, 2, 
				0, 7, 7, 
				});
		
		evalQuery("[id=0]([]([_name=a]),[_optional=true]([_optional=true]([_name=b,id<{a.id}])))", new int [] {
				0, 1, 4, 1, 3,
				0, 2, 5, 1, 3,
				0, 2, 5, 1, 4,
				});
	}
		
	@Test
	public static void testRefSetrAndOptionalComplex() throws SyntaxError {
		evalQuery("[]([]([_name=a,id<4]),[_optional=true]([_optional=true]([_name=b,id<{a.id}])))", new int [] {
				0, 1, 3, 1, 
				0, 1, 3, 2, 
				});
	}

	@Test
	public static void testRefSetr() throws SyntaxError {
		/*
		evalQuery("[]([],[])", new int [] {
				0, 1, 1,
				0, 1, 2,
				0, 1, 7,
				0, 2, 1,
				0, 2, 2,
				0, 2, 7,
				0, 7, 1,
				0, 7, 2,
				0, 7, 7,});
		*/

		evalQuery("[id=0]([_name=a],[id>{a.id}])", new int [] {
				0, 1, 2,
				0, 1, 7,
				0, 2, 7,
				});
		
		evalQuery("[id=0]([_name=a],[id={a.id}])", new int [] {
				0, 1, 1,
				0, 2, 2,
				0, 7, 7,});

	}
	
	@Test(expectedExceptions={IllegalStateException.class})
	public void testDuplicateName() throws SyntaxError {
		evalQuery("[_name=a]([_name=a])", null);
	}

	@Test
	public void testTwoOptionals() throws SyntaxError {
		String queryString = "[]([id=7],[]([id=3]),[id=1,_optional=true]([id=777,_optional=true]))";

		QueryObject obj = FSQuery.buildQuery(queryString);
		QueryData data = FSQueryTest.buidQueryObject();
		Iterable<QueryMatch> res = obj.evaluate(data);
		
		for (QueryMatch queryMatch : res) {
			System.err.println(queryMatch.getMatchingNodes());
		}

		
		evalQuery(queryString, new int [] {
				0, 7, 1, 3, 1
				});
		
		
	}

}
