/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import lexer.Token;

public class ReadExpr extends PrimaryExpr {
	
	private final Token read;

	public ReadExpr(Token read, Type type) {
		super(type);
		this.read = read;
	}

	@Override
	public void genJava(PW pw) {
		pw.print("new Scanner(System.in).");
		if(read.getValue().equals("readString"))
			pw.print("nextLine()");
		else
			pw.print("nextInt()");
	}

}
