package jw.zbus.constant;

public enum ProjectInfoType {
	accountCenter("�˻�����"), hardwareCenter("Ӳ������"),messageCenter("��Ϣ����"),
	payCenter("֧������"),caculateCenter("����"),bigFinance("�����"),decorationFinance("װ�޷���"),monthlyFinance("�¸�");

	// ö��˵��()
	private String value;

	/**
	 * ˽�еĹ��췽��
	 */
	private ProjectInfoType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
