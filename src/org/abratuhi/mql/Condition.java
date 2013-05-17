package org.abratuhi.mql;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * Condition class represents a complex ("advanced") query entered by the user.
 * </p>
 * <p>
 * Condition is a conjunction of comparisons and conditions, connected by a
 * logical operator. Only operators that are part of
 * {@link Condition.ConditionOperator} enumeration are supported.
 * </p>
 * <p>
 * Each condition must contain at least one condition or comparison to be parsed
 * and evaluated correctly.
 * </p>
 *
 * <pre>
 * {@code
 * condition ::= ( <condition_operator> (<condition>|<comparison>)[(<condition>|<comparison>)...(<condition>|<comparison>)] 
 * condition_operator ::= AND|OR|NOT 
 * comparison ::= ('<column>'<comparison_operator>"<value>")
 * comparison_operator ::= =|~
 * }
 * </pre>
 *
 * <p>
 * Adding support for new condition operators is possible in the following
 * manner:
 * <ul>
 * <li>add the new condition operator to the {@link Condition.ConditionOperator}
 * enumeration.</li>
 * <li>implement evaluation for the condition operator in
 * {@link Condition#evaluate(Map)}</li>
 * </ul>
 * </p>
 *
 * <p>
 * Important - condition operators and comparison operators are case-sensitive!
 * </p>
 *
 * @param <T>	base class of the objects in the conditions.
 *
 * @author Alexei Bratuhin
 *
 */
public class Condition<T extends Map<String, String>> implements
	IEvaluatable<T> {

    /**
     * This enumeration represents a set of available condition operators.
     * @author Alexei Bratuhin.
     *
     */
    enum ConditionOperator {
	/**
	 * Logical AND.
	 */
	AND("AND"),
	/**
	 * Logical OR.
	 */
	OR("OR"),
	/**
	 * Logical NOT.
	 */
	NOT("NOT");

	/**
	 * String representation of the logical condition operator.
	 */
	private String strco;

	/**
	 * Constructor.
	 * @param strco	logical condition operator as string
	 */
	ConditionOperator(final String strco) {
	    this.strco = strco;
	}

	@Override
	public String toString() {
	    return this.strco;
	}

	/**
	 * Create a logical condition operator from string.
	 * @param operator	logical condition operator as string
	 * @return logical condition operator.
	 * @throws ParseException	in case the string was not recognized
	 * as a logical condition operator.
	 */
	public static ConditionOperator fromString(final String operator)
		throws ParseException {
	    for (ConditionOperator co : ConditionOperator.values()) {
		if (co.toString().equals(operator)) {
		    return co;
		}
	    }

	    throw new ParseException(
		    "Unsupported condition's logical operator: " + operator
			    + "!");
	}
    };

    /**
     * Condition operator.
     */
    private ConditionOperator operator = null;
    /**
     * List of underlying conditions and comparisons
     * connected by the logical condition operator.
     */
    private List<IEvaluatable<T>> conditions = new Vector<IEvaluatable<T>>();

    @Override
    public final boolean evaluate(final T record) {
	switch (operator) {
	case AND:
	    boolean resultAnd = true;
	    for (IEvaluatable<T> condition : conditions) {
		resultAnd &= condition.evaluate(record);
	    }
	    return resultAnd;
	case OR:
	    boolean resultOr = false;
	    for (IEvaluatable<T> condition : conditions) {
		resultOr |= condition.evaluate(record);
	    }
	    return resultOr;
	case NOT:
	    boolean resultNot = false;
	    // evaluate (NOT) as (NOT(true))
	    IEvaluatable<T> eval = conditions.isEmpty() ? new True<T>()
		    : conditions.get(0); // should never occur - current parser
					 // implementation insists on every
					 // {@link Condition} to contain at
					 // least one evaluation. Still prevent
					 // IndexOutOfBoundException in case
					 // this parser behaviour will be
					 // changed in future
	    resultNot = (!eval.evaluate(record));
	    return resultNot;
	default:
	    return false;
	}
    }

    /**
     * Get logical operator joining following conditions.
     *
     * @return logical operator as string
     */
    public final String getOperator() {
	return this.operator.toString();
    }

    /**
     * Set logical operator from string.
     *
     * @param operator
     *            logical operator
     * @throws ParseException
     *             in case the logical operator could not be parsed from string
     */
    public final void setOperator(final String operator) throws ParseException {
	this.operator = ConditionOperator.fromString(operator);
    }

    /**
     * Add evaluatable to list of evaluatables joined by the logical operator.
     * @param evaluatable evaluatable to add
     */
    public final void addCondition(final IEvaluatable<T> evaluatable) {
	conditions.add(evaluatable);
    }

    @Override
    public final String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("(");
	sb.append(getOperator());
	for (IEvaluatable<T> ev : conditions) {
	    sb.append(ev.toString());
	}
	sb.append(")");
	return sb.toString();
    }

}
