Sonarqube Plugin: Count reachable instructions

This plugins counts the number of reachable instructions of a package of a
java program starting from one ore more entry points. It does this doing a
static data flow analysis on the programs execution graph via Soot.

Installation

Download the sources and compile them with maven (mvn package). Then copy the
jar file in target to the extensions/plugins directory of your Sonarqube
installation and restart it.

After that you have to enter the two settings "entry points" and "package" via
the Sonarqube project settings interface for the project to be analyzed.
Examples are given there, the package is understood "with sub packages".
Additionally you have to make sure that the sonar-project.properties of the
project to measure sets the path to the binaries via sonar.libraries and
sonar.binaries as this plugin works on compiled bytecode.

Motivation

For measuring developer performance as X per time usually lines of code is
taken as X although it has a couple of problems. Besides code formating which
can be healed by shared formating rules it depends a lot on general coding
style and the don't-repeat-yourself principle. Notably lines of code
decreases when similar code is refactored.

The intuition behind "Reachable instruction count" is that the "size" of a
program is the number of instructions that is executable via all pathes of
the flow graph. This should be what a program "can do". Using the static
single assignment form of the flow graph generated from the compiled byte
code makes sure that the count is coding style independent. Stepping
through the code instruction-by-instructions recounts a method every time
it is called so refactoring to use shared code does not decrease the
size of the program / the reachable instructions count.

Limitations

Abstract, interfaces and overridden methods have to be resolved to the actual
executed methods, just like the VM has to do it. This can be done by tracking
the actual return types of methods with abstract return types and the actual
types of variables that are not primitives. However with static analysis this
has some limitations as it is not possible to

* track return types of native or vm methods
* know the types of properties initialized by reflection

The latter is probably a problem for programs that are tied together by IoC.
Ideas how this can be solved are welcome.