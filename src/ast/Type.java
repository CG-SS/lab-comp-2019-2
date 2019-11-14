/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class Type extends ASTElement{
	
	private final String id;

	public Type(String id) {
		this.id = id;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void genJava(PW pw) {
		if(id.equals("Int")) {
			pw.printIdent("int");
		} else if(id.equals("Boolean")) {
			pw.printIdent("boolean");
		} else {
			pw.printIdent(id);
		}
	}
	
	public String getId() {
		return id;
	}
}
