/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class ParamDec extends ASTElement {
	
	private final Type type;
	private final String idName;

	public ParamDec(Type type, String idName) {
		this.type = type;
		this.idName = idName;
	}

	@Override
	public void genJava(PW pw) {
		// TODO Auto-generated method stub

	}
	
	public Type getType() {
		return type;
	}



	public String getIdName() {
		return idName;
	}

}
