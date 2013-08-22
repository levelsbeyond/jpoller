package org.sadun.util.polling.test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import org.sadun.util.polling.DirectoryPoller;
import org.sadun.util.polling.PollManager;
import org.sadun.util.polling.pollmanagers.FileHistoryPollManager;



public class Test {

    static boolean timeBased=false;
    static boolean pollingTimeBased;
    static boolean autoMove=false;
    static String dir=null;
    static String filterPattern=null;
        
    public static void main(String args[]) throws Exception {
    	
    	String pmc ="org.sadun.util.polling.test.TestPollManager";

        if (args.length == 0) {
            System.out.println("org.sadun.util.polling.Test <dir> [-tbp|-tbm][-am][-fast|-slow]");
            System.out.println();
            System.out.println("Test the directory poller over the given directory.");
            System.out.println("Polling is every 10 secs. In automove mode, the directory is ");
            System.out.println("the default one (see docs).");
            System.out.println();
            System.out.println("-tbp    time-based polling (last polling time)");
            System.out.println("-tbm    time-based polling (higher modification time)");
            System.out.println("-am     auto move mode");
            System.out.println("-fast   polls every second");
            System.out.println("-slow   polls every 100 seconds");
            System.out.println("-pm:name PollManager class name (defaults to a TestPollManager)");
            System.out.println("-f [regexp] filters based on given regexp");
            
            System.exit(-1);
        }

        long pollInterval=10000;

        for(int i=0;i<args.length;i++) {
            if ("-tbp".equals(args[i])) {
                timeBased=true;
                pollingTimeBased=true;
            }
            else if ("-tbm".equals(args[i])) {
                timeBased=true;
                pollingTimeBased=false;
            } else if ("-am".equals(args[i])) autoMove=true;
            else if ("-f".equals(args[i])) {
            	filterPattern=args[++i]; }
            else if ("-fast".equals(args[i])) pollInterval=1000;
            else if ("-slow".equals(args[i])) pollInterval=100000;
            else if (args[i].startsWith("-pm")) pmc=args[i].substring(args[i].indexOf(":")+1);
            else if (args[i].startsWith("-")) throw new RuntimeException("Unrecognized option "+args[i]);
            else dir=args[i];
        }

        if (dir==null)  {
            System.out.println("Please state the directory to poll.");
            System.exit(-1);
        }
        
        FilenameFilter filter=new DirectoryPoller.NullFilenameFilter();
        if (filterPattern!=null) {
        	final Pattern pattern = Pattern.compile(filterPattern);
        	filter=new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return pattern.matcher(name).matches();
				}
				public String toString() {
					return "filter accepting files matching "+filterPattern+"";
				}
        	};
        }
        
        DirectoryPoller poller = new DirectoryPoller(filter);
        poller.addDirectory(new File(dir));
        poller.setPollInterval(pollInterval);
        poller.setAutoMove(autoMove);
        poller.setTimeBased(timeBased);
        poller.setPollingTimeBased(pollingTimeBased);

		PollManager pollmanager = (PollManager)Class.forName(pmc).newInstance();
        poller.addPollManager(pollmanager);
        poller.addPollManager(new FileHistoryPollManager());
        poller.setSendSingleFileEvent(true);

        poller.setVerbose(true);
        poller.start();
        Thread.sleep(1000L);
        System.out.flush();
        System.out.println("Shutting down...");
        System.out.flush();
        poller.shutdown();
        long start=System.currentTimeMillis();
        System.out.flush();
        System.out.println("Waiting for termination...");
        System.out.flush();
        while(poller.isAlive());
        
        Thread.sleep(5000L);
        
        System.out.flush();
        System.out.println("Restarting...");
        System.out.flush();
        
        new Thread(poller).start();
                
        System.out.flush();
        System.out.println("Started anew");
        
        System.out.flush();
        Thread.sleep(1000L);
        System.out.flush();
        System.out.println("Main almost finished");
        System.out.flush();
        
        Thread.sleep(5000L);
        
        System.out.flush();
        System.out.println("Shutting down one last time...");
        System.out.flush();
        poller.shutdown();

        
    }

}

