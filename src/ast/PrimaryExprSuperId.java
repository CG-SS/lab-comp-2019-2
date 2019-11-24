/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class PrimaryExprSuperId extends PrimaryExpr {
	
	private final String id;

	public PrimaryExprSuperId(String id, Type type) {
		super(type);
		this.id = id;
	}

	@Override
	public void genJava(PW pw) {
		pw.print("super." + id + "()");
	}

}
