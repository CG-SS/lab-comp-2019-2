/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class ExpressionFactor extends Factor {

	final Expression expr;
	
	public ExpressionFactor(Expression expr) {
		super(expr.getType());
		this.expr = expr;
	}

	@Override
	public void genJava(PW pw) {
		pw.print("(");
		expr.genJava(pw);
		pw.print(")");
	}

	
	
}
