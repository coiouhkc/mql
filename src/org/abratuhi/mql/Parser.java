package org.abratuhi.mql;

import java.util.Map;

/**
 * <p>
 * Parser class represents the advanced query parser for advanced queries as
 * entered by the user in GUI.
 * </p>
 * <p>
 * Parser class and all other classes in the package {@link org.abratuhi.mql}
 * are independent of the application's model classes - the only requirement for
 * the underlying {@link IEvaluatable} it to implement the {@link java.util.Map}
 * interface for the {@link Comparison#evaluate(Map)} method.
 * </p>
 *
 * <p>
 * Parser is case-sensitive since its underlying parts - {@link Condition} and
 * {@link Comparison} are case-sensitive.
 * </p>
 *
 * <p>
 * Note: syntax of the advanced search query is greatly inspired by Active
 * Directory and BMC Remedy Action Request System search queries.
 * </p>
 *
 * @param <T> base class of the objects in the conditions and comparisons.
 *
 * @author Alexei Bratuhin
 *
 */
public class Parser<T extends Map<String, String>> {

    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_GENERAL = "Could not parse query";

    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_NO_MATCH_GENERAL_PATTERN = "Couldn't parse query %s - it doesn't match the regex %s";
    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_NO_MATCH_CONDITION_PATTERN = "Condition doesn't match the pattern: <operator>(<condition>)..(<condition>)";
    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_NO_MATCH_CONDITION_HAS_AT_LEAST_ONE_COMPARISON = "Condition's logical operator must be followed by an opening bracket!";
    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_NUMBER_BRACKETS = "Number of opening and closing brackets doesn't match!";
    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_NUMBER_SINGLE_QUOTES = "Number of opening and closing single quotation marks doesn't match!";
    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_NUMBER_DOUBLE_QUOTES = "Number of opening and closing double quotation marks doesn't match!";
    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_NO_LOGICAL_OPERATOR = "Couldn't find condition's logical operator!";
    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_COMPARISON_MUST_START_WITH_QUOTED_FIELD_NAME = "Comparison must start with the quoted field name!";
    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_COMPARISON_VALUE_MUST_BE_DOUBLE_QUOTED = "Comparison value must be double quoted!";
    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_FIELD_NAME_MUST_NOT_BE_EMPTY = "Field name may not be empty!";
    /**
     * Predefined string for corresponding error.
     */
    public static final String ERROR_FIELD_VALUE_MUST_NOT_BE_EMPTY = "Field value may not be empty!";
    
    private static final String ADVANCED_QUERY_GENERAL_PATTERN = "\\(.*\\)";
    private static final String ADVANCED_QUERY_COMPARISON_PATTERN = "^'(?:[^'\\\\]|\\\\.)*'.\"(?:[^'\\\\]|\\\\.)*\"$";
    
    
    private static final String CHAR_ESC = "\\";
    private static final String CHAR_START_EXPR = "(";
    private static final String CHAR_END_EXPR = ")";
    private static final String CHAR_START_KEY = "'";
    private static final String CHAR_START_VALUE = "\"";

    /**
     * @param query
     *            - advanced query as entered by the user
     * @return construction that can be evaluated,
     *         returning true/false depending on whether the particular record
     *         matches the given query.
     * @throws ParseException
     *             - in case the advanced query could not be parsed
     */
    public final IEvaluatable<T> parse(final String query)
	    throws ParseException {
	return parseCondition(query.trim());
    }

    /**
     * <p>
     * Recursive method to build the advanced query.
     * </p>
     * <p>
     * Loop through the characters one-by-one, doing some primitive pattern
     * matching beforehand, try to identify conditions and comparisons and parse
     * those in subsequent calls to {@link Parser#parseCondition(String)} and
     * {@link Parser#parseComparison(String)}
     * </p>
     *
     * @param query
     *            - advance query as entered by user or part of it identified to
     *            be a {@link Comparison}
     * @return construction that can be evaluated
     *         returning true/false depending on whether the particular record
     *         matches the given query.
     * @throws ParseException
     *             - in case the advanced query or its part could not be parsed
     */
    private IEvaluatable<T> parseCondition(final String query)
	    throws ParseException {
	if (query.matches(ADVANCED_QUERY_GENERAL_PATTERN)) {
	    // now remove trailing brackets and check whether it's a comparison
	    // or a condition
	    String innerQuery = query.substring(1, query.length() - 1);
	    if (innerQuery.matches(ADVANCED_QUERY_COMPARISON_PATTERN)) {
		// -> comparison
		return parseComparison(innerQuery);
	    } else { // -> condition
		Condition<T> condition = new Condition<T>();

		// find for first opening bracket to be able to extract the
		// logical operator
		int firstOpeningBracket = innerQuery.indexOf("(");

		// if no opening bracket found we have only a logical operator
		// without operands -> exit with exception
		if (firstOpeningBracket == -1) {
		    throw new ParseException(ERROR_NO_MATCH_CONDITION_PATTERN);
		}

		// extract logical operator
		String operator = innerQuery.substring(0, firstOpeningBracket);

		// try to set the logical operator -> exit with exception if
		// operator is not supported
		condition.setOperator(operator);

		// subInnerQuery shall contain further conditions and
		// comparisons
		String subInnerQuery = innerQuery
			.substring(firstOpeningBracket);

		// initialiaze some useful variables
		int openingBrackets = 0;
		int closingBrackets = 0;
		boolean inName = false; // whether in comparison's field name,
					// meaning between the single quotes
		boolean inValue = false; // whether in comparison's field value,
					 // meaning between the double quotes
		boolean inEscape = false; // whether previously captured
					  // character was an escape character
		int start = -1; // current group (condition or comparison) start
		int end = -1; // current group (condition or comparison) end

		// check whether there is a chance for parsing conditions and
		// comparisons - check for non-empty string starting with a
		// bracket
		if (subInnerQuery.length() == 0
			|| subInnerQuery.charAt(0) != '(') {
		    throw new ParseException(
			    ERROR_NO_MATCH_CONDITION_HAS_AT_LEAST_ONE_COMPARISON);
		}

		// iterate through all the characters, trying to identify
		// conditions and comparisons, parse those and add them to the
		// list of {@link IEvaluatable} to be evaluated against a
		// particular record.
		// Note: at any point of time openingBrackets >= closingBrackets
		for (int i = 0; i < subInnerQuery.length(); i++) {
		    char current = subInnerQuery.charAt(i);
		    switch (current) {
		    case '\\':
			inEscape = !inEscape; // switch the "escaped mode" flag
			break;
		    case '(':
			if(!inEscape) {
			    openingBrackets++;
			} else {
			    inEscape = !inEscape; // switch the "escaped mode" flag
			}
			if (start == -1) {
			    start = i; // a new matching group has started.
			}
			break;
		    case ')':
			if(!inEscape) {
			    closingBrackets++;
			} else {
			    inEscape = !inEscape; // switch the "escaped mode" flag
			}
			if (openingBrackets < closingBrackets) {
			    throw new ParseException(ERROR_NUMBER_BRACKETS);
			} else if (openingBrackets == closingBrackets) {
			    end = i; // current matching group has ended
			    // extract substring to evaluate in recursive call,
			    // whether it will be a condition or a comparison
			    String evString = subInnerQuery.substring(start,
				    end + 1);
			    IEvaluatable<T> ev = parseCondition(evString);
			    condition.addCondition(ev);

			    // reset the index counters for currently matched
			    // group
			    start = -1;
			    end = -1;

			    if (subInnerQuery.length() > (i + 1)
				    && subInnerQuery.charAt(i + 1) != '(') {
				throw new ParseException(
					ERROR_NO_MATCH_CONDITION_PATTERN);
			    }
			}

			break;
		    case '"':
			if (inEscape) { // double quote has been either escaped
			    inEscape = !inEscape;
			} else { // or indicates the switch of the
				 // "field value mode" flag
			    inValue = !inValue;
			}
			break;
		    case '\'':
			if (inEscape) { // single quote has been either escaped
			    inEscape = !inEscape;
			} else {
			    inName = !inName; // or indicates the switch of the
					      // "field name mode" flag
			}
			break;
		    default:
			if (inEscape) {
			    inEscape = !inEscape;
			}
			break;
		    }
		}

		// execute some final checks after we're out of characters to
		// proceed.

		if (openingBrackets != closingBrackets) {
		    throw new ParseException(ERROR_NUMBER_BRACKETS);
		}

		if (inName) {
		    throw new ParseException(ERROR_NUMBER_SINGLE_QUOTES);
		}

		if (inValue) {
		    throw new ParseException(ERROR_NUMBER_DOUBLE_QUOTES);
		}
		
		if (inEscape) {
		    throw new ParseException(ERROR_GENERAL);
		}

		return condition;
	    }
	} else {
	    throw new ParseException(String.format(
		    ERROR_NO_MATCH_GENERAL_PATTERN, query,
		    ADVANCED_QUERY_GENERAL_PATTERN));
	}
    }

    /**
     * @param query
     *            - advanced query substring supposed to contain a single
     *            comparison.
     * @see {@link Comparison} documentation for the exact structure of a
     *      comparison.
     * @return construction that can be evaluated
     *         returning true/false depending on whether the particular record
     *         matches the given query.
     * @throws ParseException
     *             - in case the comparison could not be parsed
     */
    private IEvaluatable<T> parseComparison(final String query)
	    throws ParseException {
	Comparison<T> comparison = new Comparison<T>();

	// comparison must not be empty and comparison must start with the
	// quoted field name
	if (query.length() == 0 || query.charAt(0) != '\'') {
	    throw new ParseException(
		    ERROR_COMPARISON_MUST_START_WITH_QUOTED_FIELD_NAME);
	}

	String query1 = query.substring(1);

	// find the index of the closing single quote. Beware of the escaped
	// single quotes - '\''
	int closingSingleQuoteIndex = -1;
	for (int i = 0; i < query1.length(); i++) {
	    if (query1.charAt(i) == '\'' && i > 0
		    && query1.charAt(i - 1) != '\\') {
		closingSingleQuoteIndex = i;
		break;
	    }
	}

	// throw {@link ParseException} in case we haven't found the closing
	// single quote.
	if (closingSingleQuoteIndex == -1) {
	    throw new ParseException(
		    ERROR_COMPARISON_MUST_START_WITH_QUOTED_FIELD_NAME);
	}

	// extract the field name
	String field = query1.substring(0, closingSingleQuoteIndex);
	field = field.replaceAll("\\\\\\\\", "\\\\");
	field = field.replaceAll("\\\\'", "'");
	comparison.setField(field);

	// field name may not be empty !
	if (field == null || field.length() == 0) {
	    throw new ParseException(ERROR_FIELD_NAME_MUST_NOT_BE_EMPTY);
	}

	// prepare a substring for further parsing
	String query2 = query1.substring(closingSingleQuoteIndex + 1);

	// Having parsed the single quoted column name, we assume the next char
	// to be the comparison operator, so try to parse it.
	String operator = query2.substring(0, 1);
	comparison.setOperator(operator);

	// Having parsed the comparison operator, try to parse the value of the
	// column in the comparison.
	String query3 = query2.substring(1);
	// field value must be double quoted and
	// overall comparison may not contain further chars
	// after end of double quoted value
	if (query3.length() == 0 || query3.charAt(0) != '\"') {
	    throw new ParseException(
		    ERROR_COMPARISON_VALUE_MUST_BE_DOUBLE_QUOTED);
	}

	int closingDoubleQuoteIndex = -1;
	for (int i = 0; i < query3.length(); i++) {
	    if (query3.charAt(i) == '\"' && i > 0
		    && query3.charAt(i - 1) != '\\') {
		closingDoubleQuoteIndex = i;
		break;
	    }
	}

	if (closingDoubleQuoteIndex == -1
		|| closingDoubleQuoteIndex != query3.length() - 1) {
	    throw new ParseException(
		    ERROR_COMPARISON_VALUE_MUST_BE_DOUBLE_QUOTED);
	}

	String value = query3.substring(1, query3.length() - 1);
	value = value.replaceAll("\\\\\\\\", "\\\\");
	value = value.replaceAll("\\\\\"", "\"");
	comparison.setValue(value);

	// once again, don't allow for null/empty values!
	// if needed implement an additional IS NULL comparison
	if (value == null || value.length() == 0) {
	    throw new ParseException(ERROR_FIELD_VALUE_MUST_NOT_BE_EMPTY);
	}

	return comparison;
    }

}
