/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;


public class Qualifier extends ASTElement {
	
	private final String qualifier;
	private final boolean isFinal;
	private final boolean isOverride;

	public Qualifier(String qualifier, boolean isFinal, boolean isOverride) {
		this.qualifier = qualifier;
		this.isFinal = isFinal;
		this.isOverride = isOverride;
	}

	@Override
	public void genJava(PW pw) {
		if(isOverride) {
			pw.println("@Override");
			pw.printIdent("");
		}
		if(isFinal) {
			pw.print("final ");
		}
		pw.print(qualifier);
	}

	public String getString() {
		return qualifier;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public boolean isOverride() {
		return isOverride;
	}

}
