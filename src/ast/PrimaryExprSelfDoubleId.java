/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class PrimaryExprSelfDoubleId extends PrimaryExpr {

	private final String idName;
	private final String secondIdName;
	
	public PrimaryExprSelfDoubleId(String idName, String secondIdName, Type type) {
		super(type);
		
		this.idName = idName;
		this.secondIdName = secondIdName;
	}

	@Override
	public void genJava(PW pw) {
		pw.print("this.");
		pw.print(idName);
		pw.print(".");
		pw.print(secondIdName);
		pw.print("()");
	}

}
