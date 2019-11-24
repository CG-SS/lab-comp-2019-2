/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.ArrayList;
import java.util.List;

import lexer.Token.Symbol;

public class ClassDec extends ASTElement {
	
	final private String className;
	private boolean openClass;
	private boolean extendsClass;
	private ClassDec superClass;
	private final List<Member> memberList;

	public ClassDec(final String className) {
		// TODO Auto-generated constructor stub
		this.className = className;
		openClass = false;
		extendsClass = false;
		superClass = null;
		memberList = new ArrayList<>();
	}

	@Override
	public void genJava(PW pw) {
		pw.print("class " + className);
		if(extendsClass) {
			pw.print(" extends " + superClass.getName());
		}
		pw.println(" {");
		pw.add();
		for(final Member m : memberList) {
			m.genJava(pw);
		}
		pw.sub();
		pw.println("}");
	}
	
	public boolean isOpen() {
		return openClass;
	}
	
	public MethodDec getLocalPublicFunction(final String id) {
		for(final Member m : memberList) {
			if(m instanceof MethodDec) {
				if(m.getId().equals(id) && m.getQualifier().getString().equals(Symbol.PUBLIC.toString())){
					return (MethodDec)m;
				}
			}
		}
		
		return null;
	}
	
	public MethodDec getPublicFunction(final String id) {
		for(final Member m : memberList) {
			if(m instanceof MethodDec) {
				if(m.getId().equals(id) && m.getQualifier().getString().equals(Symbol.PUBLIC.toString())){
					return (MethodDec)m;
				}
			}
		}
		
		if (superClass != null) {
			return superClass.getPublicFunction(id);
		}
		
		return null;
	}
	
	public MethodDec getPrivateFunction(final String id) {
		for(final Member m : memberList) {
			if(m instanceof MethodDec) {
				if(m.getId().equals(id) && m.getQualifier().getString().equals(Symbol.PRIVATE.toString())){
					return (MethodDec)m;
				}
			}
		}
		
		return null;
	}
	
	public FieldDec getPrivateVar(final String id) {
		for(final Member m : memberList) {
			if(m instanceof FieldDec) {
				if(m.getId().equals(id) && m.getQualifier().getString().equals(Symbol.PRIVATE.toString())){
					return (FieldDec)m;
				}
			}
		}
		
		return null;
	}
	
	public Member getMember(final String id) {
		for(final Member member : memberList) {
			if(member.getId().equals(id)) {
				return member;
			}
		}
		
		return null;
	}
	
	public boolean hasMember(final String id) {
		for(final Member member : memberList) {
			if(member.getId().equals(id)) {
				return true;
			}
		}
		
		return false;
	}
	
	public String getName() {
		return className;
	}
	
	public ClassDec getSuperClass() {
		return superClass;
	}
	
	public MethodDec getFunction(final String id) {
		for(final Member member : memberList) {
			if(member.getId().equals(id)) {
				if(member instanceof MethodDec) {
					return (MethodDec) member;
				}
			}
		}
		
		return null;
	}
	
	public FieldDec getVar(final String id) {
		for(final Member member : memberList) {
			if(member.getId().equals(id)) {
				if(member instanceof FieldDec) {
					return (FieldDec) member;
				}
			}
		}
		
		return null;
	}
	
	public void setOpenClass(boolean openClass) {
		this.openClass = openClass;
	}

	public void setExtendsClass(boolean extendsClass) {
		this.extendsClass = extendsClass;
	}

	public void setSuperClassName(final ClassDec superClass) {
		this.superClass = superClass;
	}
	
	public void addMember(final Member member) {
		memberList.add(member);
	}
}
