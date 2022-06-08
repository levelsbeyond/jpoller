JPoller
==========

The JPoller library was written by Critiano Sadun but has not been maintained for several years (last release was 2006).  It is a filesystem directory poller with several nice features for auto-moving and filtering built in.

A few code improvements have been put into this source fork:

* A fix to a bug could cause `FileSetFoundEvents` to not have the correct automove directory if a cycle's last file is not processed.  This was due to a `dir` directory being set to the automove directory only on the last file in the processing array, so if for any reason it was not processed the automove directory would not be set correctly and corresponding event listeners would be looking for a file that no longer existed.
* An improvement was added to always eliminate hidden files from processing candidacy.
* Added testng, junit, and mockito testing framework.
* Added the ability to set a delay on the moving or deleting of processed files and directories on a per-watch folder basis for a set number of minutes to facilitate time for post-event asynchonous processing to complete.

Original source can be found here:  [http://sourceforge.net/projects/jpoller/](http://sourceforge.net/projects/jpoller/)
