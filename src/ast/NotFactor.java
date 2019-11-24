/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class NotFactor extends Factor {
	
	private final Factor factor;

	public NotFactor(Factor factor) { // TODO bool
		super(factor.getType());
		this.factor = factor;
	}

	@Override
	public void genJava(PW pw) {
		pw.print("!");
		factor.genJava(pw);
	}

}
