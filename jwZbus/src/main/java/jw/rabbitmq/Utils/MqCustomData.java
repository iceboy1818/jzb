package jw.rabbitmq.Utils;

import java.io.Serializable;
/**
 * @author ���ΰ
 * 
 */
public class MqCustomData implements Serializable {
	
	private String projectName;

	private String modelName;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	
}
