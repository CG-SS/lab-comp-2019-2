/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

public class BasicValue extends Factor {
	
	private final String value;

	public BasicValue(String value, Type type) {
		super(type);
		this.value = value;
	}

	@Override
	public void genJava(PW pw) {
		if(this.getType().getId().equals("String")) {
			pw.print("\"" + value + "\"");
		} else {
			pw.print(value);
		}
	}

}
