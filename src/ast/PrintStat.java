/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class PrintStat extends Statement {
	
	private final boolean newline;
	private final Expression expr;
	private final List<Expression> exprList;

	public PrintStat(boolean newline, Expression expr, List<Expression> exprList) {
		this.newline = newline;
		this.expr = expr;
		this.exprList = exprList;
	}



	@Override
	public void genJava(PW pw) {
		String printFunc;
		if(newline)
			printFunc = "println";
		else
			printFunc = "print";
		
		pw.printIdent("");
		pw.print("System.out." + printFunc + "(");
		expr.genJava(pw);
		pw.println(");");
		
		if(exprList != null) {
			for(final Expression e : exprList) {
				pw.printIdent("");
				pw.print("System.out." + printFunc + "(");
				e.genJava(pw);
				pw.println(");");
			}
		}
	}

}
