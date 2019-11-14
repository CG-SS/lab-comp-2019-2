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
		if(this.getQualifier().isOverride()) {
			pw.printlnIdent("@Override");
		}
		
		String rType;
		if(this.getType() == null) {
			rType = "void";
		} else {
			rType = this.getType().getId();
		}
		
		pw.printIdent(this.getQualifier().getString());
		pw.print(" " + rType + " " + this.getId() + "(");
		if(paramList != null) {
			for(final ParamDec pd : paramList) {
				pd.genJava(pw);
			}
		}
		pw.println(") {");
		pw.add();
		for(final Statement stat : statList) {
			stat.genJava(pw);
		}
		pw.sub();
		pw.printlnIdent("}");
	}

	public List<ParamDec> getParameters() {
		return paramList;
	}
	
	public void setStatList(final List<Statement> stats) {
		this.statList = stats;
	}
}
