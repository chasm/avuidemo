JAVAC = javac
JAVAC_FLAGS = -sourcepath .

JAVA = java
JAVA_FLAGS =

CUP = java_cup.Main
CUPFLAGS = -nopositions -expect 100

JLEX = JLex.Main

LATEX = latex
DVIPS = dvips

all: test WhiteRabbit.ps

test: absyn com/biosimilarity/scalate/model/WhiteRabbit/Yylex.class com/biosimilarity/scalate/model/WhiteRabbit/PrettyPrinter.class com/biosimilarity/scalate/model/WhiteRabbit/Test.class com/biosimilarity/scalate/model/WhiteRabbit/ComposVisitor.class com/biosimilarity/scalate/model/WhiteRabbit/AbstractVisitor.class com/biosimilarity/scalate/model/WhiteRabbit/FoldVisitor.class com/biosimilarity/scalate/model/WhiteRabbit/AllVisitor.class com/biosimilarity/scalate/model/WhiteRabbit/parser.class com/biosimilarity/scalate/model/WhiteRabbit/sym.class com/biosimilarity/scalate/model/WhiteRabbit/Test.class

.PHONY: absyn

%.class: %.java
	${JAVAC} ${JAVAC_FLAGS} $^

absyn: com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Expression.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Sequence.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Application.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Mention.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Value.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Abstraction.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Stipulation.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/VariableExpr.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Transcription.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/AtomLiteral.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/ValueExpr.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Numeric.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/ListVariableExpr.java
	${JAVAC} ${JAVAC_FLAGS} $^

com/biosimilarity/scalate/model/WhiteRabbit/Yylex.java: com/biosimilarity/scalate/model/WhiteRabbit/Yylex
	${JAVA} ${JAVA_FLAGS} ${JLEX} com/biosimilarity/scalate/model/WhiteRabbit/Yylex

com/biosimilarity/scalate/model/WhiteRabbit/sym.java com/biosimilarity/scalate/model/WhiteRabbit/parser.java: com/biosimilarity/scalate/model/WhiteRabbit/WhiteRabbit.cup
	${JAVA} ${JAVA_FLAGS} ${CUP} ${CUPFLAGS} com/biosimilarity/scalate/model/WhiteRabbit/WhiteRabbit.cup
	mv sym.java parser.java com/biosimilarity/scalate/model/WhiteRabbit/

com/biosimilarity/scalate/model/WhiteRabbit/Yylex.class: com/biosimilarity/scalate/model/WhiteRabbit/Yylex.java com/biosimilarity/scalate/model/WhiteRabbit/sym.java

com/biosimilarity/scalate/model/WhiteRabbit/sym.class: com/biosimilarity/scalate/model/WhiteRabbit/sym.java

com/biosimilarity/scalate/model/WhiteRabbit/parser.class: com/biosimilarity/scalate/model/WhiteRabbit/parser.java com/biosimilarity/scalate/model/WhiteRabbit/sym.java

com/biosimilarity/scalate/model/WhiteRabbit/PrettyPrinter.class: com/biosimilarity/scalate/model/WhiteRabbit/PrettyPrinter.java

WhiteRabbit.dvi: WhiteRabbit.tex
	${LATEX} WhiteRabbit.tex

WhiteRabbit.ps: WhiteRabbit.dvi
	${DVIPS} WhiteRabbit.dvi -o WhiteRabbit.ps

clean:
	 rm -f com/biosimilarity/scalate/model/WhiteRabbit/Absyn/*.class com/biosimilarity/scalate/model/WhiteRabbit/*.class
	 rm -f .dvi WhiteRabbit.aux WhiteRabbit.log WhiteRabbit.ps  *.class

vclean:
	 rm -f com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Expression.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Sequence.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Application.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Mention.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Value.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Abstraction.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Stipulation.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/VariableExpr.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Transcription.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/AtomLiteral.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/ValueExpr.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Numeric.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/ListVariableExpr.java com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Expression.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Sequence.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Application.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Mention.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Value.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Abstraction.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Stipulation.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/VariableExpr.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Transcription.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/AtomLiteral.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/ValueExpr.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/Numeric.class com/biosimilarity/scalate/model/WhiteRabbit/Absyn/ListVariableExpr.class
	 rm -f com/biosimilarity/scalate/model/WhiteRabbit/Absyn/*.class
	 rmdir com/biosimilarity/scalate/model/WhiteRabbit/Absyn/
	 rm -f WhiteRabbit.tex WhiteRabbit.dvi WhiteRabbit.aux WhiteRabbit.log WhiteRabbit.ps 
	 rm -f com/biosimilarity/scalate/model/WhiteRabbit/Yylex com/biosimilarity/scalate/model/WhiteRabbit/WhiteRabbit.cup com/biosimilarity/scalate/model/WhiteRabbit/Yylex.java com/biosimilarity/scalate/model/WhiteRabbit/VisitSkel.java com/biosimilarity/scalate/model/WhiteRabbit/ComposVisitor.java com/biosimilarity/scalate/model/WhiteRabbit/AbstractVisitor.java com/biosimilarity/scalate/model/WhiteRabbit/FoldVisitor.java com/biosimilarity/scalate/model/WhiteRabbit/AllVisitor.java com/biosimilarity/scalate/model/WhiteRabbit/PrettyPrinter.java com/biosimilarity/scalate/model/WhiteRabbit/Skeleton.java com/biosimilarity/scalate/model/WhiteRabbit/Test.java com/biosimilarity/scalate/model/WhiteRabbit/sym.java com/biosimilarity/scalate/model/WhiteRabbit/parser.java com/biosimilarity/scalate/model/WhiteRabbit/*.class
	 rm -f Makefile
	 rmdir -p com/biosimilarity/scalate/model/WhiteRabbit/

