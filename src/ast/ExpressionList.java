/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class ExpressionList extends ASTElement {
	
	private final Expression expr;
	private final List<Expression> exprList;

	public ExpressionList(Expression expr, List<Expression> exprList) {
		this.expr = expr;
		this.exprList = exprList;
	}

	@Override
	public void genJava(PW pw) {
		expr.genJava(pw);
		pw.println(", ");
		for(final Expression e : exprList) {
			e.genJava(pw);
		}
	}

}
