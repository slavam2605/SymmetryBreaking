## Choco3 ##

Choco3 is an open-source Java library for Constraint Programming.

This document reports the release process, the version number 3.1.0 should be adapted.

1. Make sure the code is stable, bug free, etc.

2. Check maven dependencies, update if necessary, and clean also (using archiva f-ex.)

    $ mvn versions:display-dependency-updates

3. Generate PDF documentation

    $ cd docs/
    $ make latexpdf

4. Check that ALL issues are reported in CHANGES.md files

5. Mount the /Volumes/choco-repo samba point (required to upload files for maven)

6. Prepare and run the release using the script
	
	$ sh release.sh request	
	$ sh release.sh perform
	$ sh release.sh zip

7. Publish choco-repo/ intranet to internet

8. Upload the zip file onto the website

===================
The Choco3 dev team.
