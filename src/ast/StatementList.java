/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class StatementList extends ASTElement {
	
	private final List<Statement> statList;

	public StatementList(List<Statement> statList) {
		this.statList = statList;
	}

	@Override
	public void genJava(PW pw) {
		for(final Statement s : statList) {
			s.genJava(pw);
		}
	}
	
	public List<Statement> getStatList(){
		return statList;
	}

}
