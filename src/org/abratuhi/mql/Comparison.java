package org.abratuhi.mql;

import java.util.Map;

/**
 * <p>
 * Comparison class represents a simple check on the value of a particular
 * column.
 * </p>
 * <p>
 * Comparison consists of a column name, a comparison operator and a value. Only
 * comparison operators from the {@link Comparison.ComparisonOperator}
 * enumeration are allowed.
 * </p>
 *
 * <pre>
 * {@code
 * comparison ::= ('<column>'<comparison_operator>"<value>")
 * comparison_operator ::= =|~
 * }
 * </pre>
 *
 * <p>
 * Note: <column> and <value> may contain ' and " respectively - they need to be
 * escaped as \' and \".
 * </p>
 *
 * <p>
 * Adding support for new comparison operators is possible in the following
 * manner:
 * <ul>
 * <li>add the new comparison operator to the
 * {@link Comparison.ComparisonOperator} enumeration.</li>
 * <li>implement evaluation for the comparison operator in
 * {@link Comparison#evaluate(Map)}</li>
 * </ul>
 * </p>
 *
 * <p>
 * Important - condition operators and comparison operators are case-sensitive!
 * </p>
 *
 * @param <T> base class of the objects being compared.
 *
 * @author Alexei Bratuhin
 *
 */
public class Comparison<T extends Map<String, String>> implements
	IEvaluatable<T> {

    /**
     * This enumeration represents a set of available comparison operators.
     * @author Alexei Bratuhin
     *
     */
    enum ComparisonOperator {
	/**
	 * Equality comparison operator.
	 */
	EQUAL("="),
	/**
	 * Likeliness (in terms of regular expressions - match)
	 * comparison operator.
	 */
	LIKE("~");

	/**
	 * String representation of the comparison operator.
	 */
	private String comco;

	/**
	 * Default constructor.
	 * @param comco	comparison operation
	 */
	ComparisonOperator(final String comco) {
	    this.comco = comco;
	}

	@Override
	public String toString() {
	    return this.comco;
	}

	/**
	 * Create a comparison operator from string.
	 * @param operator comparison operator as string
	 * @return comparison operator
	 * @throws ParseException	in case the string was not
	 * recognized as comparison operator
	 */
	public static ComparisonOperator fromString(final String operator)
		throws ParseException {
	    for (ComparisonOperator co : ComparisonOperator.values()) {
		if (co.toString().equals(operator)) {
		    return co;
		}
	    }

	    throw new ParseException("Unsupported comparison operator: "
		    + operator + "!");
	}

    };

    /**
     * Comparison operator.
     */
    private ComparisonOperator operator;

    /**
     * Name of the field to compare.
     */
    private String field;
    /**
     * Value of the field used for comparison.
     */
    private String value;

    @Override
    public final boolean evaluate(final T record) {
	String recordvalue = record.get(field);
	boolean result = false;
	switch (operator) {
	case EQUAL:
	    if (recordvalue != null && recordvalue.equals(value)) {
		result = true;
	    }
	    break;
	case LIKE:
	    if (recordvalue != null && recordvalue.matches(value)) {
		result = true;
	    }
	    break;
	default:
	    break;
	}
	return result;
    }

    /**
     * Get field name.
     *
     * @return field name
     */
    public final String getField() {
	return field;
    }

    /**
     * Get field value.
     *
     * @return field value
     */
    public final String getValue() {
	return value;
    }

    /**
     * Get operator between field name and field value.
     *
     * @return operator
     */
    public final String getOperator() {
	return this.operator.toString();
    }

    /**
     * Set operator.
     *
     * @param op
     *            operator
     * @throws ParseException
     *             in case the operator string could not be parsed
     */
    public final void setOperator(final String op) throws ParseException {
	this.operator = ComparisonOperator.fromString(op);
    }

    /**
     * Set field name.
     *
     * @param field
     *            field name
     */
    public final void setField(final String field) {
	this.field = field;
    }

    /**
     * Set field value.
     *
     * @param value
     *            field value
     */
    public final void setValue(final String value) {
	this.value = value;
    }

    @Override
    public final String toString() {
	return String.format("('%s'%s\"%s\")", getField().replaceAll("'", "\\\\'"), getOperator(),
		getValue().replaceAll("\"", "\\\\\""));
    }

}
