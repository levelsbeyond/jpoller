package org.sadun.util;

import java.util.Comparator;

/**
 * This comparator returns a value which depends on the combination
 * of two other comparators.
 * <p>
 * The first comparator is used to determine an higher-level ordering;
 * the second, a lower-level ordering within the first level.
 * <p>
 * If two objects compare equal according to the first comparator, then the
 * second is used.
 *
 * @author Cristiano Sadun
 */
public class ChainedComparator implements Comparator {

    protected Comparator comp1;
    protected Comparator comp2;
    
    /**
     * 
     */
    public ChainedComparator(Comparator comp1, Comparator comp2) {
       this.comp1=comp1;
       this.comp2=comp2;
    }

    /** If two objects compare equal according to the first comparator, then the
     * second is used.	
     */
    public int compare(Object o1, Object o2) {
        int c1= comp1.compare(o1,o2);
        if (c1==0) return comp2.compare(o1,o2);
        else return c1;
    }
    
    public String toString() {
        return comp1.toString()+" then "+comp2;
    }

}
