/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class OperatorTerm extends Term {
	
	private final SignalFactor signalFactor;
	private final List<HighOperator> opList;
	private final List<SignalFactor> signalList;

	public OperatorTerm(SignalFactor signalFactor, List<HighOperator> opList, List<SignalFactor> signalList, Type type) {
		super(type);
		
		this.signalFactor = signalFactor;
		this.opList = opList;
		this.signalList = signalList;
	}

	@Override
	public void genJava(PW pw) {
		signalFactor.genJava(pw);
		
		for(int i = 0; i < opList.size(); i++) {
			pw.print(" ");
			opList.get(i).genJava(pw);
			pw.print(" ");
			signalList.get(i).genJava(pw);
		}
	}

}
