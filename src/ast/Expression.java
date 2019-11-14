/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public abstract class Expression extends ASTElement {
	
	private final Type type;

	public Expression(Type type) {
		// TODO Auto-generated constructor stub
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
}
