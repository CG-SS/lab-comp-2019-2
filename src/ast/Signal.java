/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import lexer.Token.Symbol;

public class Signal extends ASTElement {
	
	private final Symbol signal;

	public Signal(Symbol signal) {
		this.signal = signal;
	}

	@Override
	public void genJava(PW pw) {
		pw.print(signal.toString());
	}

}
