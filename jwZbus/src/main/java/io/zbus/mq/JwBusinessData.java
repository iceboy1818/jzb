package io.zbus.mq;

import java.util.List;
import java.util.Map;

/*-
 * yinajiwe
 */
public class JwBusinessData {

	private Map<String, Object> params;

	private String businessModel;

	private JwBusinessDataHeadInfo JwBusinessDataHeadInfo;
	
	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
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
