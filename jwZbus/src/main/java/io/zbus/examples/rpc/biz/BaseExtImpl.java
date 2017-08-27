package io.zbus.examples.rpc.biz;

public class BaseExtImpl implements IBaseExt {

	
	public User save(User user) {
		System.out.println(user.getName());
		return user;
	}  

}
