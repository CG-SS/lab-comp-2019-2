/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class MethodDec extends Member {
	
	private final List<ParamDec> paramList;
	private List<Statement> statList;

	public MethodDec(String idColon, List<ParamDec> formalParamDec, Type type, Qualifier qualifier) {
		super(idColon, qualifier, type);
		
		this.paramList = formalParamDec;
	}

	@Override
	public void genJava(PW pw) {
		pw.printIdent("");
		this.getQualifier().genJava(pw);
		if(this.getType() == null) {
			pw.print(" void ");
		} else {
			pw.print(" ");
			this.getType().genJava(pw);
			pw.print(" ");
		}
		
		if(paramList != null) {
			final StringBuilder sb = new StringBuilder();
			sb.append("_").append(this.getId());
			sb.setLength(sb.toString().length() - 1);
			
			pw.print(sb.toString() + " (");
			
			for(int i = 0; i < paramList.size(); i++) {
				paramList.get(i).genJava(pw);
				if(i != paramList.size() - 1) {
					pw.print(", ");
				}
			}
		} else {
			pw.print(this.getId() + " (");
		}
		pw.println(") {");
		pw.add();
		
		for(final Statement s : statList) {
			s.genJava(pw);
		}
		pw.sub();
		pw.printIdent("");
		pw.println("}");
	}

	public List<ParamDec> getParameters() {
		return paramList;
	}
	
	public void setStatList(final List<Statement> stats) {
		this.statList = stats;
	}
}
