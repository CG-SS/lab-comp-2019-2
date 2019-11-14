/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class SignalFactor extends Term {
	
	private final Factor factor;
	private final Signal signal;

	public SignalFactor(Signal signal, Factor factor, Type type) {
		super(type);
		// TODO Auto-generated constructor stub
		this.factor = factor;
		this.signal = signal;
	}

	@Override
	public void genJava(PW pw) {
		if(signal != null) {
			signal.genJava(pw);
		}
		factor.genJava(pw);
	}

	public Factor getFactor() {
		return factor;
	}
}
