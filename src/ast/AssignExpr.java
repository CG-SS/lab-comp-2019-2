/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class AssignExpr extends Statement {
	
	private final Expression expr;
	private final Expression assignExpr;

	public AssignExpr(Expression expr, Expression assignExpr) {
		// TODO Auto-generated constructor stub
		this.expr = expr;
		this.assignExpr = assignExpr;
	}

	@Override
	public void genJava(final PW pw) {
		pw.printIdent("");
		expr.genJava(pw);
		
		if(assignExpr != null){
			pw.print(" = ");
			assignExpr.genJava(pw);
		}
		pw.println(";");
	}

}
