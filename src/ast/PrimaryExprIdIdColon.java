/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class PrimaryExprIdIdColon extends PrimaryExpr {
	
	private final String idName;
	private final String idColonName;
	private final List<Expression> exprList;

	public PrimaryExprIdIdColon(String idName, String idColonName, List<Expression> exprList, Type type) {
		super(type);
		
		this.idName = idName;
		this.idColonName = idColonName;
		this.exprList = exprList;
	}

	@Override
	public void genJava(PW pw) {
		final StringBuilder sb = new StringBuilder();
		sb.append("_").append(idColonName);
		sb.setLength(sb.toString().length() - 1);
		
		pw.print(idName);
		pw.print(".");
		pw.print(sb.toString());
		pw.print("(");
		
		for(int i = 0; i < exprList.size(); i++) {
			exprList.get(i).genJava(pw);
			if(i != exprList.size() - 1)
				pw.print(", ");
		}
		pw.print(")");
	}

}
