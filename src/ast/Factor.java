/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public abstract class Factor extends ASTElement {
	
	private final Type type;

	public Factor(Type type) {
		// TODO Auto-generated constructor stub
		this.type = type;
	}

	public Type getType() {
		return type;
	}
}
