package io.zbus.mq;

import java.util.List;
import java.util.Map;

/*-
 * yinajiwe
 */
public class JwBusinessData {

	private Map<String, Object> datas;

	private String businessModel;

	private JwBusinessDataHeadInfo JwBusinessDataHeadInfo;
	
	

	public Map<String, Object> getDatas() {
		return datas;
	}

	public void setDatas(Map<String, Object> datas) {
		this.datas = datas;
	}

	public String getBusinessModel() {
		return businessModel;
	}

	public void setBusinessModel(String businessModel) {
		this.businessModel = businessModel;
	}

	public JwBusinessDataHeadInfo getJwBusinessDataHeadInfo() {
		return JwBusinessDataHeadInfo;
	}

	public void setJwBusinessDataHeadInfo(JwBusinessDataHeadInfo jwBusinessDataHeadInfo) {
		JwBusinessDataHeadInfo = jwBusinessDataHeadInfo;
	}
	
	
}
