JCC = javac

JFLAGS = -g

JRUN = java

PICO = pico

default: Project2compilers.class

#-------------------------------------
Project2compilers.class: Project2compilers.java
	$(JCC) $(JFLAGS) Project2compilers.java
#-------------------------------------
clean:
	$(RM) *.class

run:
	$(JRUN) Project2compilers testfile.txt

