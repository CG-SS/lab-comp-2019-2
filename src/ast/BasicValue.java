/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class BasicValue extends ASTElement {
	
	private final String value;

	public BasicValue(String value) {
		this.value = value;
	}

	@Override
	public void genJava(PW pw) {
		// TODO Auto-generated method stub

	}

}
