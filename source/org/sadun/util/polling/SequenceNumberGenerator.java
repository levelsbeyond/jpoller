package org.sadun.util.polling;

/**
 * Classes implementing this interface can generate JMX notification sequence numbers.
 *
 * @author Cristiano Sadun
 */
public interface SequenceNumberGenerator {
    
    /**
     * Return the next sequence number.
     * <p>
     * This method may or may not be threadsafe, depending on the implementation
     * class.
     * 
     * @return the next sequence number.
     */
    public long getNextSequenceNumber();

}
