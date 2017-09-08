package io.zbus.mq;

import java.util.Date;

public class JwBusinessDataHeadInfo {
	
	private String message ;
	
	private String  code;
	
	private Date createTime;
	
	private String fromProjectName;

	private String[] toProjectName;

	private String environment;

	private String ip;

	private boolean isBroad;// 是否是广播

	private boolean isSingle;// 是否点对对

	private boolean isRpc; // 是否RPC

	private boolean needCallBack; // 是否需要回调

	private String rpcId;

	private String broadId;

	private String callBackSequenceId;
	
	
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getFromProjectName() {
		return fromProjectName;
	}

	public void setFromProjectName(String fromProjectName) {
		this.fromProjectName = fromProjectName;
	}

	public String[] getToProjectName() {
		return toProjectName;
	}

	public void setToProjectName(String[] toProjectName) {
		this.toProjectName = toProjectName;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isBroad() {
		return isBroad;
	}

	public void setBroad(boolean isBroad) {
		this.isBroad = isBroad;
	}

	public boolean isSingle() {
		return isSingle;
	}

	public void setSingle(boolean isSingle) {
		this.isSingle = isSingle;
	}

	public boolean isRpc() {
		return isRpc;
	}

	public void setRpc(boolean isRpc) {
		this.isRpc = isRpc;
	}

	public boolean isNeedCallBack() {
		return needCallBack;
	}

	public void setNeedCallBack(boolean needCallBack) {
		this.needCallBack = needCallBack;
	}

	

	public String getRpcId() {
		return rpcId;
	}

	public void setRpcId(String rpcId) {
		this.rpcId = rpcId;
	}

	public String getBroadId() {
		return broadId;
	}

	public void setBroadId(String broadId) {
		this.broadId = broadId;
	}

	public String getCallBackSequenceId() {
		return callBackSequenceId;
	}

	public void setCallBackSequenceId(String callBackSequenceId) {
		this.callBackSequenceId = callBackSequenceId;
	}
	
	
}
