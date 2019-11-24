/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class AssertStat extends Statement {
	
	private final Expression expr;
	private final String message;

	

	public AssertStat(Expression expr, String message) {
		this.expr = expr;
		this.message = message;
	}

	@Override
	public void genJava(PW pw) {
		pw.printIdent("");
		pw.print("if(!(");
		expr.genJava(pw);
		pw.println(")) System.out.println(\"" + message + "\");");
	}

}
