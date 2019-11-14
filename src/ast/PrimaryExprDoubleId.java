/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class PrimaryExprDoubleId extends PrimaryExpr {
	
	private final String firstId;
	private final String secondId;

	public PrimaryExprDoubleId(String idName, String secondIdName, Type type) {
		super(type);
		// TODO Auto-generated constructor stub
		this.firstId = idName;
		this.secondId = secondIdName;
	}

	@Override
	public void genJava(PW pw) {
		pw.println(firstId + "." + secondId + "();");
	}

}
