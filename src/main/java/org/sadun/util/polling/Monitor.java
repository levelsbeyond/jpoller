package org.sadun.util.polling;

/**
 * A very simple example application that shows the directory monitor in action
 *
 * @author C. Sadun
 * @version 1.0
 */
public class Monitor {

//    private transient CmdLineOptions co;
//    private File [] files;
//    private JPanelOutputStream jps;
//    private PrintStream ps;
//    private DirectoryPoller poller;
//
//    private class MonitorPollManager extends BasePollManager {
//        public void fileSetFound(FileSetFoundEvent evt) {
//            File [] polledFiles = evt.getFiles();
//            for(int i=0;i<polledFiles.length;i++) {
//                ps.println("Polled "+polledFiles[i].getAbsolutePath());
//            }
//        }
//    }
//
//    public Monitor() {
//        co=new CmdLineOptions();
//        co.setOnOption("files", this, "files");
//        co.setDescription("files","The directories to monitor");
//        co.setMandatory("files", true);
//    }
//
//    public void init(String args[]) throws IOException {
//        co.parse(args);
//        for(int i=0;i<files.length;i++) {
//            if (! files[i].isDirectory())
//                throw new Error(files[i].getCanonicalPath()+" is not a directory.");
//            if (! files[i].exists())
//                System.err.println("Warning: "+files[i].getCanonicalPath()+" does not exist.");
//        }
//
//        jps= new JPanelOutputStream();
//        ps = new PrintStream(jps);
//    }
//
//    public void run() {
//        JFrame f = new JFrame("Directory monitor");
//        f.setSize(300, 300);
//        f.getContentPane().setLayout(new BorderLayout());
//        f.getContentPane().add(jps.getPanel());
//        f.addWindowListener(new WindowAdapter() {
//            public void WindowClosing(WindowEvent e) {
//                System.err.println("Shutting down poller");
//                poller.shutdown();
//                //while(poller.isAlive());
//                System.err.println("Exiting");
//                System.exit(0);
//            }
//
//        });
//
//        poller = new DirectoryPoller(files);
//        poller.addPollManager(new MonitorPollManager());
//        f.setVisible(true);
//        poller.start();
//
//    }
//
//    public static void main(String args[]) throws Exception {
//    	try {
//	        Monitor m = new Monitor();
//	        m.init(args);
//	        m.run();
//    	} catch (CmdLineOptions.OptionException e) {
//    		System.err.println(e);
//    	}
//    }

}