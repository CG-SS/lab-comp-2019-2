/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public abstract class Member extends ASTElement {
	
	private final String id;
	private final Qualifier qualifier;
	private final Type type;
	
	public Member(String id, Qualifier qualifier, Type type) {
		super();
		this.id = id;
		this.qualifier = qualifier;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public Qualifier getQualifier() {
		return qualifier;
	}

	public Type getType() {
		return type;
	}
}
