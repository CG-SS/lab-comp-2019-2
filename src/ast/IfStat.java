/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class IfStat extends Statement {

	private final Expression expr;
	private final StatementList ifStatList;
	private final StatementList elseStatList;
	
	public IfStat(Expression expr, StatementList ifStatList, StatementList elseStatList) {
		this.expr = expr;
		this.ifStatList = ifStatList;
		this.elseStatList = elseStatList;
	}

	@Override
	public void genJava(PW pw) {
		pw.printIdent("");pw.print("if(");
		expr.genJava(pw);
		pw.println(") {");
		pw.add();
		ifStatList.genJava(pw);
		pw.sub();
		pw.printIdent("");
		pw.println("}");
		
		if(elseStatList != null) {
			pw.printIdent("");
			pw.println("else {");
			pw.add();
			elseStatList.genJava(pw);
			pw.sub();
			pw.printIdent("");
			pw.println("}");
		}
	}

}
