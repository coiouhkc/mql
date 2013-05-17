package org.abratuhi.mql;

/**
 * <p>
 * This class represents the exception thrown when the {@link Parser} was not
 * able to parse the advanced search query, meaning the advanced search query as
 * entered by the user is invalid and cannot be evaluated
 * </p>
 * <p>
 * Note: in cases the advances query cannot be evaluated, an empty set could be
 * returned as result of the corresponding evaluation - please check the exact
 * implementation.
 * </p>
 *
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class ParseException extends Exception {

    /**
     * Default constructor.
     */
    public ParseException() {
	super();
    }

    /**
     * Default constructor with error message.
     *
     * @param message
     *            error message
     */
    public ParseException(final String message) {
	super(message);
    }

}
