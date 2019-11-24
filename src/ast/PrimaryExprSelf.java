/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class PrimaryExprSelf extends PrimaryExpr {

	public PrimaryExprSelf(Type type) {
		super(type);
	}

	@Override
	public void genJava(PW pw) {
		pw.print("this");
	}

}
