/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class BreakStat extends Statement {

	@Override
	public void genJava(PW pw) {
		pw.printIdent("");
		pw.println("break;");
	}

}
