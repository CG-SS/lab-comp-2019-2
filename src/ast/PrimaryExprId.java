/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class PrimaryExprId extends PrimaryExpr {
	
	private final String id;

	public PrimaryExprId(String idName, Type type) {
		// TODO Auto-generated constructor stub
		super(type);
		
		this.id = idName;
	}

	@Override
	public void genJava(PW pw) {
		pw.print(id);
	}

	public String getId() {
		return id;
	}
}
