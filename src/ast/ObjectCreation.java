/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class ObjectCreation extends Factor {
	
	public ObjectCreation(String idName) {
		// TODO Auto-generated constructor stub
		super(new Type(idName));
	}

	@Override
	public void genJava(PW pw) {
		pw.print("new " + this.getType().getId() + "()");
	}

}
