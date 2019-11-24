/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class ReturnStat extends Statement {
	
	private final Expression expr;

	public ReturnStat(Expression exp) {
		this.expr = exp;
	}

	@Override
	public void genJava(PW pw) {
		pw.printIdent("");
		pw.print("return ");
		expr.genJava(pw);
		pw.println(";");
	}

}
