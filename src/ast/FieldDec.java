/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class FieldDec extends Member {

	public FieldDec(String id, Qualifier qualifier, Type type) {
		super(id, qualifier, type);
	}

	@Override
	public void genJava(PW pw) {
		pw.printlnIdent("private " + this.getType().getId() + " " + this.getId() + ";");
	}

	
}
