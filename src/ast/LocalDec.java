/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class LocalDec extends Statement {
	
	private final Type type;
	private final List<String> idList;

	public LocalDec(Type type, List<String> idList, Expression expression) {
		// TODO Auto-generated constructor stub
		this.type = type;
		this.idList = idList;
	}

	@Override
	public void genJava(PW pw) {
		pw.printIdent("");
		type.genJava(pw);
		pw.print(" ");
		for(int i = 0; i < idList.size(); i++) {
			pw.print(idList.get(i));
			if(i != idList.size() - 1) {
				pw.print(", ");
			}
		}
		pw.println(";");
	}

}
