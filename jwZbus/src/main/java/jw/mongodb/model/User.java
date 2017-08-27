package jw.mongodb.model;

public class User {
	
	public User(String name){
		this.Name=name;
	}
	private String Name;

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}
	
	
	
}
