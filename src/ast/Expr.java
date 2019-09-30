/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class Expr extends Statement {
    public Expr(AssignExpr assignExpr, IfStat ifStat, WhileStat whileStat, ReturnStat returnStat, PrintStat printStat,
			RepeatStat repeatStat, LocalDec localDec, AssertStat assertStat, String brk) {
		super(assignExpr, ifStat, whileStat, returnStat, printStat, repeatStat, localDec, assertStat, brk);
		// TODO Auto-generated constructor stub
	}
}