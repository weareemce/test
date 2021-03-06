package com.iv.operation.script.dto;

import com.iv.operation.script.constant.OperatingSystemType;
import com.iv.operation.script.constant.ScriptSourceType;

import java.util.List;

public class SingleTaskDto {

	private Integer taskId;
	private String taskName;// 作业任务名
	private String taskDescription;// 任务描述
	private ScriptSourceType scriptSrc;// 执行脚本来源
	private List<String> scriptArgs;// 脚本执行传入参数
	private int timeout;// 目标主机连接超时时间(毫秒)
	private OperatingSystemType systemType;// 操作系统

//	public SingleTaskDto(Integer taskId, String taskName, String taskDescription, ScriptSourceType scriptSrc,
//			List<String> scriptArgs, int timeout) {
//		super();
//		this.taskId = taskId;
//		this.taskName = taskName;
//		this.taskDescription = taskDescription;
//		this.scriptSrc = scriptSrc;
//		this.scriptArgs = scriptArgs;
//		this.timeout = timeout;
//	}

	public SingleTaskDto(Integer taskId, String taskName, String taskDescription, ScriptSourceType scriptSrc, List<String> scriptArgs, int timeout, OperatingSystemType systemType) {
		this.taskId = taskId;
		this.taskName = taskName;
		this.taskDescription = taskDescription;
		this.scriptSrc = scriptSrc;
		this.scriptArgs = scriptArgs;
		this.timeout = timeout;
		this.systemType = systemType;
	}

	public OperatingSystemType getSystemType() {
		return systemType;
	}

	public void setSystemType(OperatingSystemType systemType) {
		this.systemType = systemType;
	}

	public SingleTaskDto() {
		super();
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public String getTaskDescription() {
		return taskDescription;
	}
	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}
	public ScriptSourceType getScriptSrc() {
		return scriptSrc;
	}
	public void setScriptSrc(ScriptSourceType scriptSrc) {
		this.scriptSrc = scriptSrc;
	}
	public List<String> getScriptArgs() {
		return scriptArgs;
	}
	public void setScriptArgs(List<String> scriptArgs) {
		this.scriptArgs = scriptArgs;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
}
