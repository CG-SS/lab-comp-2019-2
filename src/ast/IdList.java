/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class IdList extends ASTElement {
	
	private final List<Id> idList;

	public IdList(List<Id> idList) {
		this.idList = idList;
	}

	@Override
	public void genJava(PW pw) {
		// TODO Auto-generated method stub

	}

	public List<Id> getIdList() {
		return idList;
	}

}
