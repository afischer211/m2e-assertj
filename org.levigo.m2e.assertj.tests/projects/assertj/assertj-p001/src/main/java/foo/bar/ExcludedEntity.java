package foo.bar;

import java.util.List;

public class ExcludedEntity {
	private String foo;
	private int bar;

	private List<String> baz;
	
	public float yada1;

	public String getFoo() {
		return foo;
	}

	public void setFoo(String foo) {
		this.foo = foo;
	}

	public int getBar() {
		return bar;
	}

	public void setBar(int bar) {
		this.bar = bar;
	}

	public List<String> getBaz() {
		return baz;
	}

	public void setBaz(List<String> baz) {
		this.baz = baz;
	}
}
