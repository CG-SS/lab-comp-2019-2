/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class RepeatStat extends Statement {
	
	private final StatementList statList;
	private final Expression expr;

	public RepeatStat(StatementList statList, Expression expr) {
		this.statList = statList;
		this.expr = expr;
	}

	@Override
	public void genJava(PW pw) {
		pw.printIdent("");
		pw.println("do {");
		pw.add();
		statList.genJava(pw);
		pw.sub();
		pw.printIdent("");
		pw.println("}");
		pw.printIdent("");
		pw.print("while (!(");
		expr.genJava(pw);
		pw.println("));");
	}

}
