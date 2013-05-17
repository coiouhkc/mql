MQL - My Query Language
==
Overview
--
This query language is greatly inspired by `ldap-search` and `ARS API` allowing to search for records in a database, where each record is a `Map` - a set of `key-value` pairs.

Query Structure
--

	condition ::= ( <condition_operator> (<condition>|<comparison>) [(<condition>|<comparison>)...(<condition>|<comparison>)]
	condition_operator ::= AND|OR|NOT
	comparison ::= ('<column>'<comparison_operator>"<value>")
	comparison_operator ::= =|~

Here `~` is the match operator, checking whether the value *matches* the given regular expression.

Usage
--
	class Record extends HashMap<String, String> {
	}
	...
	String query = "(AND(OR('address'=\"London\")('location'=\"Hogwarts\"))(OR('name'~\"Potter\")('name'~\"McGonagoll\")))";
	Parser<Record> parser = new Parser<Record>();
	IEvaluatable<Record> eval = parser.parse(query);
	
	Record r = new Record();
	r.put("address", "London");
	r.put("name", "Weasley");

	eval.evaluate(r); // returns false

	r.put("name", "Potter");
	eval.evaluate(r); // returns true


Example
--
Imagine having a database of records about a company's employees, where each record contains information about an employee's age, name, address, profession, hobby and salary.

If we would like to find all Java developers in the company, we would create following query:

	(AND('profession'="Java Developer"))
To identify all employees living in London or named Smith

	(OR('address'~"London, .*")('name'="Smith")

Imagine we would like to organize a football match between people working for company in Dublin and London, but would reserve all potential chess players for the match with another company taking place on the same date:

	(AND(OR('address'~"London, .*")('address'="Dublin"))('hobby'~"football|soccer")(NOT('hobby'="chess")))
