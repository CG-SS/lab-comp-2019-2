/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class WhileStat extends Statement {
	
	private final Expression expr;
	private final StatementList statList;

	public WhileStat(Expression expr, StatementList statList) {
		this.expr = expr;
		this.statList = statList;
	}



	@Override
	public void genJava(PW pw) {
		pw.printIdent("");
		pw.print("while (");
		expr.genJava(pw);
		pw.println(") {");
		pw.add();
		statList.genJava(pw);
		pw.sub();
		pw.printIdent("");
		pw.println("}");
	}

}
