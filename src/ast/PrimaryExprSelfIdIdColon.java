/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class PrimaryExprSelfIdIdColon extends PrimaryExpr {

	private final String idName;
	private final String idColonName;
	private final List<Expression> exprList;
	
	public PrimaryExprSelfIdIdColon(String idName, String idColonName, List<Expression> exprList, Type type) {
		super(type);
		
		this.idColonName = idColonName;
		this.idName = idName;
		this.exprList = exprList;
	}

	@Override
	public void genJava(PW pw) {
		final StringBuilder sb = new StringBuilder();
		sb.append("_").append(idColonName);
		sb.setLength(sb.toString().length() - 1);
		
		pw.print("this." + idName + "." + sb.toString() + "(");
		for(int i = 0; i < exprList.size(); i++) {
			exprList.get(i).genJava(pw);
			if(i != exprList.size() - 1)
				pw.print(", ");
		}
		pw.print(")");
	}

}
