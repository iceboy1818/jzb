
package jw.rabbitmq.Mq.data;

import jw.rabbitmq.Utils.MqCustomData;

/**
 * @author “Ûº—Œ∞
 * 
 */
public class MqCustomPushMessageData extends MqCustomData {

	private String sendFromMail;

	private String sendContent;

	private String sendSubject;

	private String sendToMail;

	private Boolean batchSend;

	public String getSendFromMail() {
		return sendFromMail;
	}

	public void setSendFromMail(String sendFromMail) {
		this.sendFromMail = sendFromMail;
	}

	public String getSendContent() {
		return sendContent;
	}

	public void setSendContent(String sendContent) {
		this.sendContent = sendContent;
	}

	public String getSendSubject() {
		return sendSubject;
	}

	public void setSendSubject(String sendSubject) {
		this.sendSubject = sendSubject;
	}

	public String getSendToMail() {
		return sendToMail;
	}

	public void setSendToMail(String sendToMail) {
		this.sendToMail = sendToMail;
	}

	public Boolean getBatchSend() {
		return batchSend;
	}

	public void setBatchSend(Boolean batchSend) {
		this.batchSend = batchSend;
	}

}
