package org.sadun.util;

import java.io.File;
import java.util.Comparator;

/**
 * 
 *
 * @author Cristiano Sadun
 */
public abstract class BidirectionalComparator implements Comparator {
    
    protected boolean ascending;
    
    /**
     * Create a comparator which will impose an ascending or descending order on
     * modification times depending on the value of the parameter
     * 
     * @param ascending if <b>true</b>, older files will be ordered before newer files.
     */
    protected BidirectionalComparator(boolean ascending) {
        this.ascending=ascending;
    }
    
    private void checkTypes(Object o1, Object o2) {
        if (!(o1 instanceof File && o2 instanceof File))
            throw new RuntimeException(
                    "Invalid object passed for comparison: expected (File, File), received ("
                            + o1.getClass().getName() + ", "
                            + o2.getClass().getName() + ")");
    }
    
    /** 
     * Expects the two objects to be of File class and returns the ordering indicator based on the
     * direction specified at construction 
     */
    public final int compare(Object o1, Object o2) {
        checkTypes(o1,o2);
        File f1 = (File)o1;
        File f2 = (File)o2;
        return computeResultFromValue(getComparisonValue(f1,f2));
    }
    
    /**
     * Must return the comparation value (0, greater than 0 or lower than 0) for
     * the two files.
     * 
     * @return the comparation value (0, greater than 0 or lower than 0) for
     * the two files.
     */
    protected abstract long getComparisonValue(File f1,  File f2);

    /**
     * Computes the result taking into account the construction parameters.
     * 
     * @param value comparation value
     * @return the final comparation value
     */
    protected int computeResultFromValue(long value) {
        return value == 0 ? 0 : value > 0 ? ascending ? 1 : -1 : ascending ? -1 : 1;
    }
}
