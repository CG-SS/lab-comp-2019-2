/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class BooleanValue extends ASTElement {
	
	private final String value;

	public BooleanValue(String value) {
		this.value = value;
	}

	@Override
	public void genJava(PW pw) {
		pw.print(value);
	}

}
