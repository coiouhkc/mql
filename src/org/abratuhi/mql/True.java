package org.abratuhi.mql;

/**
 * <p>
 * This class represents an {@link IEvaluatable} that always evaluates to
 * <code>true</code>
 * </p>
 * 
 * @author Alexei Bratuhin
 * 
 * @param <T>
 */
public class True<T> implements IEvaluatable<T> {

    @Override
    public boolean evaluate(T record) {
	return true;
    }

}
