/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class EmptyStat extends Statement {

	@Override
	public void genJava(PW pw) {
		pw.println(";");
	}

	
	
}
