/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import lexer.Token.Symbol;

public class NilFactor extends Factor {

	public NilFactor() {
		super(new Type(Symbol.NIL.toString()));
		// TODO Auto-generated constructor stub
	}

	@Override
	public void genJava(PW pw) {
		// TODO Auto-generated method stub
		
	}

	
	
}
