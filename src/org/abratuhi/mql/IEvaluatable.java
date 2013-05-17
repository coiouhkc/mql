package org.abratuhi.mql;

/**
 * <p>
 * Generic interface to represent a very general requirement for the record to
 * match a certain criteria.
 * </p>
 * <p>
 * Each class implementing this interface must define further requirements on
 * the generic class T, if needed.
 * </p>
 *
 * @author Alexei Bratuhin
 *
 * @param <T>
 */
public interface IEvaluatable<T> {

    /**
     * Evaluate the given object against current criteria.
     *
     * @param record
     *            - object to evaluate against current criteria.
     * @return <ul>
     *         <li><code>true</code> - in case given record matches current
     *         criteria</li>
     *         <li><code>false</code> - otherwise</li>
     *         </ul>
     */
    public boolean evaluate(final T record);

}
