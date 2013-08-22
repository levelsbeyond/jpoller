package org.sadun.util.polling.test;

import org.sadun.util.polling.PollManager;
import org.sadun.util.polling.PollManagerFactory;
import org.sadun.util.polling.pollmanagers.LoggerHistoryPollManager;

/**
 * A {@link org.sadun.util.polling.PollManagerFactory PollManagerFactory} which produces one
 * instance of {@link org.sadun.util.polling.test.TestPollManager TestPollManager}
 * 
 * @author cris
 */
public class TestPollManagerFactory implements PollManagerFactory {

	/**
	 * @see org.sadun.util.polling.PollManagerFactory#createPollManagers()
	 */
	public PollManager[] createPollManagers(String name) {
		// In this particular factory, the MBean name is not used
		System.out.println("Creating testPollManager for MBean <"+name+">");
		return new PollManager[] { new TestPollManager(), new LoggerHistoryPollManager("test_f") };
	}

	/**
	 * @see org.sadun.util.polling.PollManagerFactory#getDescription()
	 */
	public String getDescription() {
		return "A sample factory producing one instance of org.sadun.util.polling.test.TestPollManager";
	}

}
