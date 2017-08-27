package jw.rabbitmq.Mq.data;

import jw.rabbitmq.Utils.MqCustomData;

/**
 * @author “Ûº—Œ∞
 * 
 */
public class MqInnjiaAccountCreateData extends MqCustomData {
	
	private String mobile ;
	
	private String openId;
	
	private String realName;
	
	private String idCard;
	
	private String address;
	
	private String email;
	
	private String innjiaAccountProjectName;
	
	
	
	public String getInnjiaAccountProjectName() {
		return innjiaAccountProjectName;
	}

	public void setInnjiaAccountProjectName(String innjiaAccountProjectName) {
		this.innjiaAccountProjectName = innjiaAccountProjectName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
	

}
