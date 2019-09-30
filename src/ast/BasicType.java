/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class BasicType extends ASTElement {

	private final String type;
	
	public BasicType(final String string) {
		type = string;
	}

	@Override
	public void genJava(PW pw) {
		// TODO Auto-generated method stub

	}

}
