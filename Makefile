CP=lightup-1.0-deps.jar:.
MAINCLASS=edu.kit.iti.formal.lights.TestRunner

# The test runner supports following system properties:
# -Drunner.nocheck=true
#     If this flag is true, then testing of a found solution is skipped.
#
# -Drunner.svg=true
#      If this flag is true, SVG files are created for every game instance.
#
# -Drunner.riddles=<FOLDER>
#     Folder with riddles instances. Default: "./riddles".
#
# -Drunner.timeout=<SECONDS>
#     The timeout of the runner in seconds. Default: 5 minutes.

test: compile
	java -Drunner.svg=true  -cp $(CP) $(MAINCLASS)

benchmark: compile
	java -Drunner.nocheck=true -cp $(CP) $(MAINCLASS)

compile:
	javac -cp $(CP) MyLightUpSolver.java
