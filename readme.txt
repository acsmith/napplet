Useful Links
============

NApplet thread at processing.org
-----------------------
http://processing.org/discourse/yabb2/YaBB.pl?num=1272389054

Zip download, Wiki, and source code links at GitHub
----------------------- 
http://github.com/acsmith/napplet/downloads
http://wiki.github.com/acsmith/napplet/
http://github.com/acsmith/napplet

Notes on using the source code
==============================

(Any feedback from people who try to download and/or compile the source would be great; I'm new to this whole publishing-source-code thing.)

1) The source includes the various configuration dotfiles generated by Eclipse, so if you're using Eclipse it should be a simple matter to just create a "New Java Project" using the napplet source code and have everything work out.  The only possible hitch is that I used the Proclipsing add-on for Eclipse to start the project, so you may need to have that installed to make it work.

2) If you look in src/napplet/ and see:

Napplet.java
NappletManager.java

then you want to rename those to 

NApplet.java
NAppletManager.java

(i.e., capitalize the "A" in "NApplet".)  This is the result of a rename that for some reason didn't take in Git.  You should only need to do this once.
