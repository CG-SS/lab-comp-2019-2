/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class PrimaryExprSelfId extends PrimaryExpr {
	
	private final boolean isMethod;
	private final boolean isField;
	private final String id;

	public PrimaryExprSelfId(String idName, Type type, boolean isMethod, boolean isField) {
		// TODO Auto-generated constructor stub
		super(type);
		this.isMethod = isMethod;
		this.isField = isField;
		this.id = idName;
	}

	@Override
	public void genJava(PW pw) {
		// TODO Auto-generated method stub

	}

	public boolean isMethod() {
		return isMethod;
	}

	public boolean isField() {
		return isField;
	}

	public String getId() {
		return id;
	}
}
