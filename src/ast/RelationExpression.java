/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class RelationExpression extends Expression {

	private final SimpleExpression leftSimpleExpr;
	private final Relation relation;
	private final SimpleExpression rightSimpleExpr;
	
	public RelationExpression(SimpleExpression leftSimpleExpr, Relation relation, SimpleExpression rightSimpleExpr, Type type) {
		super(type);
		
		this.leftSimpleExpr = leftSimpleExpr;
		this.relation = relation;
		this.rightSimpleExpr = rightSimpleExpr;
	}

	@Override
	public void genJava(PW pw) {
		leftSimpleExpr.genJava(pw);
		pw.print(" ");
		relation.genJava(pw);
		pw.print(" ");
		rightSimpleExpr.genJava(pw);
	}

}
