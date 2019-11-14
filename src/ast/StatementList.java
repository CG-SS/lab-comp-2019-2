/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class StatementList extends ASTElement {
	
	private final List<Statement> statList;

	public StatementList(List<Statement> statList) {
		// TODO Auto-generated constructor stub
		this.statList = statList;
	}

	@Override
	public void genJava(PW pw) {
		// TODO Auto-generated method stub
		
	}
	
	public List<Statement> getStatList(){
		return statList;
	}

}
