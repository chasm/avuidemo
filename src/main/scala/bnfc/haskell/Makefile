all:
	happy -gca ParWhiteRabbit.y
	alex -g LexWhiteRabbit.x
	latex DocWhiteRabbit.tex; dvips DocWhiteRabbit.dvi -o DocWhiteRabbit.ps
	ghc --make TestWhiteRabbit.hs -o TestWhiteRabbit
clean:
	-rm -f *.log *.aux *.hi *.o *.dvi
	-rm -f DocWhiteRabbit.ps
distclean: clean
	-rm -f DocWhiteRabbit.* LexWhiteRabbit.* ParWhiteRabbit.* LayoutWhiteRabbit.* SkelWhiteRabbit.* PrintWhiteRabbit.* TestWhiteRabbit.* AbsWhiteRabbit.* TestWhiteRabbit ErrM.* SharedString.* WhiteRabbit.dtd XMLWhiteRabbit.* Makefile*

