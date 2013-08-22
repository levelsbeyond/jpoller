package org.sadun.util.polling;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

import org.sadun.util.EnvironmentVariables;

/**
 * A {@link PollManager PollManager} that hands over the actual 
 * handling of polling events to an external script.
 * <p>
 * The scripts are executed by invoking <tt>System.execute()</tt>
 * with a given shell command.
 * <p>
 * By default, win32 invocation uses <tt>%COMMSPEC%</tt>
 * and Unix invocation uses <tt>/bin/sh</tt>. 
 * 
 * @version 1.0
 * @author cris
 */
public class ScriptedPollManager extends BasePollManager {
	
	private String [] shellCmd;
	private String scriptPath;
	private DirectoryPoller poller;
	
	public ScriptedPollManager(DirectoryPoller poller, 
	                            String [] shellCmd, String scriptPath) {
	    this.poller=poller;
		this.shellCmd=shellCmd;
		this.scriptPath=scriptPath;
	}
	
	public ScriptedPollManager(DirectoryPoller poller, 
		                       String scriptPath) throws UnsupportedOperationException {
		this.poller=poller;		                       	
		this.scriptPath=scriptPath;
		String osName=System.getProperty("os.name");
		if (osName.startsWith("Windows")) {
			String comSpec=EnvironmentVariables.getInstance().getEnv("ComSpec");
			if (comSpec==null) throw new UnsupportedOperationException("ComSpec environment variable not defined");
			this.shellCmd=new String[2];
			this.shellCmd[0]=comSpec;
			this.shellCmd[1]="/C";
		} else if (		
		   osName.equals("Linux") ||
		   osName.equals("Solaris") ||
		   osName.equals("HP-UX") ||
		   osName.equals("Aix") ||
		   osName.equals("FreeBSD")
		 ) {
			this.shellCmd=new String[1];
			this.shellCmd[0]="/bin/sh";
			
		}
		if (shellCmd==null) 
			throw new UnsupportedOperationException("The scripted poll manager doesn't recognize the operating system \""+osName+"\" and cannot determine the shell command to use");
	}
	
	private String composeCmdString(String []params) {
		String [] params2 = new String[params.length+shellCmd.length+1];
		System.arraycopy(shellCmd, 0, params2, 0, shellCmd.length);
		params2[shellCmd.length]=scriptPath;
		System.arraycopy(params, 0, params2, shellCmd.length+1, params.length);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for(int i=0;i<params2.length;i++) {
			pw.print(params2[i]);
			pw.print(" ");
		}
		pw.print(scriptPath);
		return sw.toString();
	}
	
    private void invoke(String []params) {
    	if (poller.isShuttingDown()) return;
		String [] params2 = new String[params.length+shellCmd.length+1];
		System.arraycopy(shellCmd, 0, params2, 0, shellCmd.length);
		params2[shellCmd.length]=scriptPath;
		System.arraycopy(params, 0, params2, shellCmd.length+1, params.length);
		try {
			if (poller.isVerbose()) {
				System.err.println("Attempting to run "+composeCmdString(params));
			}
			Process p = Runtime.getRuntime().exec(params2);
			InputStream os = p.getInputStream();
			InputStream os2 = p.getErrorStream();
			int c;
			try {
				while((c=os.read())!=-1) System.out.write(c);
			} catch(IOException e) {
				System.err.println("Problem reading script output for "+composeCmdString(params));
			}
			
			try {
				while((c=os2.read())!=-1) System.err.write(c);
			} catch(IOException e) {
				System.err.println("Problem reading script error output for "+composeCmdString(params));
			}
			
			try {
				int result = p.waitFor();
				if (result != 0) 
					System.err.println(composeCmdString(params)+" returned nonzero");
			} catch(InterruptedException e) {
				System.err.println("ScriptedPollManager interrupted while waiting for child process "+composeCmdString(params));
			}
		} catch (IOException e) {
			System.err.println("Problem invoking "+composeCmdString(params));
		}
	}
	
	public String toString() { 
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.print("Scripted pollManager running ");
		for(int i=0;i<shellCmd.length;i++) {
			pw.print(shellCmd[i]);
			pw.print(" ");
		}
		pw.print(scriptPath);
		return sw.toString();
	}

	/**
	 * @see org.sadun.util.polling.PollManager#cycleEnded(org.sadun.util.polling.CycleEndEvent)
	 */
	public void cycleEnded(CycleEndEvent evt) {
		invoke(new String [] {
			"CycleEnded", 
			String.valueOf(evt.getTime())
			}
		);
	}

	/**
	 * @see org.sadun.util.polling.PollManager#cycleStarted(org.sadun.util.polling.CycleStartEvent)
	 */
	public void cycleStarted(CycleStartEvent evt) {
		invoke(new String [] {
			"CycleStarted", 
			String.valueOf(evt.getTime())
			}
		);
	}

	/**
	 * @see org.sadun.util.polling.PollManager#directoryLookupEnded(org.sadun.util.polling.DirectoryLookupEndEvent)
	 */
	public void directoryLookupEnded(DirectoryLookupEndEvent evt) {
		try {
			invoke(new String [] {
				"DirectoryLookupEnded", 
				DateFormat.getDateTimeInstance().format(new Date(evt.getTime())),
				evt.getDirectory().getCanonicalPath()
				}
			);
		} catch (IOException e) {
			e.printStackTrace(System.err);
			System.err.println("Problem in getting the canonical path for <directory lookup end> event");
		}
	}

	/**
	 * @see org.sadun.util.polling.PollManager#directoryLookupStarted(org.sadun.util.polling.DirectoryLookupStartEvent)
	 */
	public void directoryLookupStarted(DirectoryLookupStartEvent evt) {
		try {
			invoke(new String [] {
				"DirectoryLookupStarted", 
				DateFormat.getDateTimeInstance().format(new Date(evt.getTime())),
				evt.getDirectory().getCanonicalPath()
				}
			);
		} catch (IOException e) {
			e.printStackTrace(System.err);
			System.err.println("Problem in getting the canonical path for <directory lookup start> event");
		}
	}

	/**
	 * @see org.sadun.util.polling.PollManager#exceptionDeletingTargetFile(java.io.File)
	 */
	public void exceptionDeletingTargetFile(File target) {
		try {
			invoke(new String [] {
				"ExceptionDeletingTargetFile", 
				target.getCanonicalPath()
				}
			);
		} catch (IOException e) {
			e.printStackTrace(System.err);
			System.err.println("Problem in getting the canonical path for <exception deleting target file> event");
		}
	}

	/**
	 * @see org.sadun.util.polling.PollManager#exceptionMovingFile(java.io.File, java.io.File)
	 */
	public void exceptionMovingFile(File file, File dest) {
		try {
			invoke(new String [] {
				"ExceptionMovingTargetFile", 
				file.getCanonicalPath(),
				dest.getCanonicalPath()
				}
			);
		} catch (IOException e) {
			e.printStackTrace(System.err);
			System.err.println("Problem in getting the canonical path for <exception moving target file> event");
		}
	}

	/**
	 * @see org.sadun.util.polling.PollManager#fileFound(org.sadun.util.polling.FileFoundEvent)
	 */
	public void fileFound(FileFoundEvent evt) {
		try {
			invoke(new String [] {
				"FileFound", 
				DateFormat.getDateTimeInstance().format(new Date(evt.getTime())),
				evt.getDirectory().getCanonicalPath(),
				evt.getFile().getCanonicalPath(),
				}
			);
		} catch (IOException e) {
						e.printStackTrace(System.err);
			System.err.println("Problem in getting the canonical path for <file found> event");
		}
	}

	/**
	 * @see org.sadun.util.polling.PollManager#fileMoved(org.sadun.util.polling.FileMovedEvent)
	 */
	public void fileMoved(FileMovedEvent evt) {
		try {
			invoke(new String [] {
				"FileMoved", 
				DateFormat.getDateTimeInstance().format(new Date(evt.getTime())),
				evt.getOriginalPath().getCanonicalPath(),
				evt.getMovedFile().getDestinationPath().getCanonicalPath()
				}
			);
		} catch (IOException e) {
						e.printStackTrace(System.err);
			System.err.println("Problem in getting the canonical path for <file moved> event");
		}
	}
	
	/**
	 * @see org.sadun.util.polling.PollManager#fileSetFound(org.sadun.util.polling.FileSetFoundEvent)
	 */
	public void fileSetFound(FileSetFoundEvent evt) {
		try {
			File [] files = evt.getFiles();
			if (files.length==0) return;
			String [] params = new String[files.length+3];
			
			params[0]="FileSetFound";			params[1]=DateFormat.getDateTimeInstance().format(new Date(evt.getTime()));
			params[2]=evt.getDirectory().getCanonicalPath();
			for(int i=0;i<files.length;i++) {
				params[i+3]=files[i].getCanonicalPath();
			}
			invoke(params);
		} catch (IOException e) {
						e.printStackTrace(System.err);
			System.err.println("Problem in getting the canonical path for <file moved> event");
		}
	}
	
	public static void main(String args[]) throws Exception {
		DirectoryPoller poller = new DirectoryPoller(new File("c:\\temp"));
		ScriptedPollManager pm = new ScriptedPollManager(poller, "c:\\test.bat");
		poller.addPollManager(pm);
		poller.setAutoMove(true);
		poller.setPollInterval(1000);
		poller.start();
	}

}
