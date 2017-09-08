package jw.zbus.constant;

public enum ProjectInfoType {
	accountCenter("账户中心"), hardwareCenter("硬件中心"),messageCenter("消息中心"),
	payCenter("支付中心"),caculateCenter("结算"),bigFinance("大金融"),decorationFinance("装修分期"),monthlyFinance("月付");

	// 枚举说明()
	private String value;

	/**
	 * 私有的构造方法
	 */
	private ProjectInfoType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
