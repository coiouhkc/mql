package org.abratuhi.mql;

import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;

public class ParserTest extends TestCase{
    
    @SuppressWarnings("serial")
    class Record extends HashMap<String, String> {
	
    }
    
    private IEvaluatable<Record> testParser(String query) {
	return testParser(query, null);
    }
    
    private IEvaluatable<Record> testParser(String query, String errorMsg) {
	IEvaluatable<Record> result = null;
	try {
	    Parser<Record> parser = new Parser<Record>();
	    result = parser.parse(query);
	    String query1 = result.toString();

	    assertEquals(query, query1);
	} catch (ParseException e) {
	    if (errorMsg != null){
		assertEquals(errorMsg, e.getMessage());
	    } else {
		System.out.println(e.getMessage());
		fail();
	    }
	}
	
	return result;
    }

    @Test
    public void testParser0() {
	String query = "('name'=\"name1\")";
	testParser(query);
    }

    @Test
    public void testParser1() {
	String query = "(AND('name'=\"name1\")('value'=\"value2\"))";
	testParser(query);
    }

    @Test
    public void testParser2() {
	String query = "(AND('name'=\"name1\")('value'=\"value2\")";
	testParser(query, Parser.ERROR_NUMBER_BRACKETS);
    }

    @Test
    public void testParser3() {
	String query = "(AND)";
	testParser(query, Parser.ERROR_NO_MATCH_CONDITION_PATTERN);
    }

    @Test
    public void testParser4() {
	String query = "(AND(''=\"\"))";
	testParser(query, Parser.ERROR_COMPARISON_MUST_START_WITH_QUOTED_FIELD_NAME);
    }

    @Test
    public void testParser5() {
	String query = "(AND('name'=\"name1\")('value'=\"value2\")(OR('age'=\"11\")('city'=\"Hobbitville\")))";
	IEvaluatable<Record> eval = testParser(query);
	
	Record r = new Record();
	
	r.put("name", "name1");
	r.put("value", "value2");
	r.put("age", "11");
	
	assertTrue(eval.evaluate(r));
	
	r.put("age", "12");
	r.put("city", "Hobbitville");
	
	assertTrue(eval.evaluate(r));
	
	r.put("city", "Townsville");
	
	assertFalse(eval.evaluate(r));
    }

    @Test
    public void testParser6() {
	String query = "(AND('name'=\"name1\")AND('value'=\"value2\"))";
	testParser(query, Parser.ERROR_NO_MATCH_CONDITION_PATTERN);
    }

    @Test
    public void testParser7() {
	String query = "(AND('name'=\"nam\\\"e1\"))";
	IEvaluatable<Record> eval = testParser(query);
	
	Record r1 = new Record();
	
	r1.put("name", "nam\"e1");
	assertTrue(eval.evaluate(r1));
	
	r1.put("name", "name1");
	assertFalse(eval.evaluate(r1));
	
    }

    @Test
    public void testParser8() {
	String query = "(AND('nam\\'e'=\"name1\"))";
	testParser(query);
    }

    @Test
    public void testParser9() {
	String query = "(AND('nam\\'e'=\"nam\\\"e1\"))";
	testParser(query);
    }
    
    @Test
    public void testParser10() {
	String query = "(AND('))";
	testParser(query, Parser.ERROR_NO_MATCH_CONDITION_PATTERN);
    }
    
    @Test
    public void testParser11() {
	String query = "(AND('a'=\"))";
	testParser(query, Parser.ERROR_NO_MATCH_CONDITION_PATTERN);
    }
    
    @Test
    public void testParser12() {
	String query = "(AND(NOT('nam\\'e'=\"nam\\\"e1\")))";
	testParser(query);
    }
    
    @Test
    public void testParser13() {
	String query = "(NOT('nam\\'e'=\"nam\\\"e1\"))";
	testParser(query);
    }
    
    @Test
    public void testParser14() {
	String query = "(AND(AND('nam\\'e'=\"nam\\\"e1\")))";
	testParser(query);
    }
    
    @Test
    public void testParser15() {
	String query = "(AND(OR('age'=\"11\")('city'=\"Hobbitville\"))('name'=\"name1\")('value'=\"value2\"))";
	testParser(query);
    }
    
    @Test
    public void testParser16() {
	String query = "(AND(OR('location'=\"Smallville\")('location'=\"Metropolis\"))('name'~\"Dogs.*\")('name'~\".*Tools.*\"))";
	testParser(query);
    }
    
    @Test
    public void test0() {
	String query = "('smoking'=\"N\")('size'=\"4\")";
	testParser(query, "Unsupported condition's logical operator: 'smoking'=\"N\")!");
    }
    
    @Test
    public void test1() {
	String query = "(AND('smoking'=\"N\")('rate'~\"\\$140.*\"))";
	IEvaluatable<Record> eval = testParser(query);
	
	assertNotNull(eval);
	
	Record r1 = new Record();
	
	r1.put("smoking", "N");
	r1.put("rate", "$140.50");
	assertTrue(eval.evaluate(r1));
	
	r1.put("smoking", "Y");
	r1.put("rate", "$140.50");
	assertFalse(eval.evaluate(r1));
	
	r1.put("smoking", "N");
	r1.put("rate", "140.50");
	assertFalse(eval.evaluate(r1));
	
    }

}
