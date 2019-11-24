/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.*;
import comp.CompilationError;

public class Program {

	public Program(final ArrayList<ClassDec> classDecList, ArrayList<MetaobjectAnnotation> metaobjectCallList, 
			       ArrayList<CompilationError> compilationErrorList) {
		this.metaobjectCallList = metaobjectCallList;
		this.compilationErrorList = compilationErrorList;
		this.classDecList = classDecList;
	}

	public void addClassDec(final ClassDec classDec) {
		classDecList.add(classDec);
	}

	public void genJava(PW pw) {
		pw.println("public class " + mainJavaClassName + " {");
		pw.add();
		pw.printIdent("");
		pw.println("public static void main(String[] args) {");
		pw.add();
		pw.printIdent("");
		pw.println("new Program().run();");
		pw.sub();
		pw.printIdent("");
		pw.println("}");
		pw.sub();
		pw.printIdent("");
		pw.println("}");
		
		for(final ClassDec classDec : classDecList) {
			classDec.genJava(pw);
		}
	}

	public void genC(PW pw) {
	}
	
	public void setMainJavaClassName(String mainJavaClassName) {
		this.mainJavaClassName = mainJavaClassName;
	}

		/**
		the name of the main Java class when the
		code is generated to Java. This name is equal
		to the file name (without extension)
		*/
	private String mainJavaClassName;


	public ArrayList<MetaobjectAnnotation> getMetaobjectCallList() {
		return metaobjectCallList;
	}
	

	public boolean hasCompilationErrors() {
		return compilationErrorList != null && compilationErrorList.size() > 0 ;
	}

	public ArrayList<CompilationError> getCompilationErrorList() {
		return compilationErrorList;
	}

	private ArrayList<MetaobjectAnnotation> metaobjectCallList;
	
	ArrayList<CompilationError> compilationErrorList;
	
	final ArrayList<ClassDec> classDecList;

	
}