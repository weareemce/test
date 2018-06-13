package com.iv.operation.script.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.iv.common.util.spring.JWTUtil;
import com.iv.operation.script.dao.impl.SingleTaskDaoImpl;
import com.iv.operation.script.dao.impl.SingleTaskLifeDaoImpl;
import com.iv.operation.script.dao.impl.SingleTaskTargetDaoImpl;
import com.iv.operation.script.dto.HostDto;
import com.iv.operation.script.dto.OptResultDto;
import com.iv.operation.script.dto.SingleTaskDto;
import com.iv.operation.script.dto.SingleTaskQueryDto;
import com.iv.operation.script.dto.TargetHostsDto;
import com.iv.operation.script.entity.SingleTaskTargetEntity;
import com.iv.operation.script.entity.SingleTaskEntity;
import com.iv.operation.script.entity.SingleTaskLifeEntity;
import com.iv.operation.script.feign.client.IScriptServiceClient;
import com.iv.operation.script.util.ErrorMsg;
import com.iv.operation.script.util.SSHAccount;
import com.iv.operation.script.util.SSHExecutor;
import com.iv.operation.script.util.SSHSessionFactory;
import com.iv.operation.script.util.ThreadPoolUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Service
public class OperationScriptService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationScriptService.class);

	@Autowired
	private SingleTaskDaoImpl singleTaskDao;
	@Autowired
	private SingleTaskTargetDaoImpl singleTaskTargetDao;
	@Autowired
	private SingleTaskLifeDaoImpl singleTaskLifeDao;
	@Autowired
	private IScriptServiceClient scriptServiceClient;

	/**
	 * 存储单脚本任务
	 * 
	 * @param dto
	 * @param scriptId
	 */
	private SingleTaskEntity taskSaveOrUpdate(SingleTaskDto dto, int scriptId) {
		SingleTaskEntity scriptEntity = null;
		SingleTaskLifeEntity lifeEntity = null;
		String user = JWTUtil.getReqValue("realName");
		long date = System.currentTimeMillis();
		if (null != dto.getTaskId()) {
			scriptEntity = singleTaskDao.selectById(dto.getTaskId());
			lifeEntity = scriptEntity.getTaskLife();
		} else {
			scriptEntity = new SingleTaskEntity();
			lifeEntity = new SingleTaskLifeEntity();
			lifeEntity.setCreaDate(date);
			lifeEntity.setCreator(user);
			scriptEntity.setTaskLife(lifeEntity);
		}
		scriptEntity.setId(dto.getTaskId());
		scriptEntity.setScriptId(scriptId);
		scriptEntity.setScriptSrc(dto.getScriptSrc());
		scriptEntity.setScriptArgs(dto.getScriptArgs());
		scriptEntity.setTaskDescription(dto.getTaskDescription());
		scriptEntity.setTaskName(dto.getTaskName());
		scriptEntity.setTimeout(dto.getTimeout());
		lifeEntity.setModDate(date);
		lifeEntity.setModifier(user);
		singleTaskDao.save(scriptEntity);

		return scriptEntity;
	}

	/**
	 * 任务创建：来源于脚本库
	 * 
	 * @param dto
	 * @param scriptId
	 */
	public SingleTaskEntity singleTaskCreate(SingleTaskDto dto, int scriptId) {
		return taskSaveOrUpdate(dto, scriptId);
	}

	/**
	 * 任务创建：来源于用户本地文件
	 * 
	 * @param dto
	 * @param file
	 * @throws Exception
	 */
	public SingleTaskEntity singleTaskCreate(SingleTaskDto dto, MultipartFile file){
		InputStream fileStream;
		try {
			fileStream = file.getInputStream();
		} catch (IOException e) {
			LOGGER.error("文件流读取失败", e);
			return null;
		}
		String fileName = file.getOriginalFilename();
		String scriptType = fileName.substring(fileName.lastIndexOf(".") + 1);
		int scriptId = scriptServiceClient.tempWrite(fileName, scriptType, fileStream);
		return taskSaveOrUpdate(dto, scriptId);
	}

	/**
	 * 任务创建：来源于用户在线录入
	 * 
	 * @param dto
	 * @param context
	 */
	public SingleTaskEntity singleTaskCreate(SingleTaskDto dto, String context, String scriptType) {
		InputStream fileStream = new ByteArrayInputStream(context.getBytes());
		int scriptId = scriptServiceClient.tempWrite(null, scriptType, fileStream);
		return taskSaveOrUpdate(dto, scriptId);
	}

	/**
	 * 单脚本任务编辑
	 * 
	 * @param dto
	 * @param scriptId
	 * @param file
	 * @param context
	 * @param scriptType
	 * @return
	 */
	public SingleTaskEntity singleTaskModify(SingleTaskDto dto, int taskId, int scriptId, MultipartFile file,
			String context, String scriptType) {

		switch (dto.getScriptSrc()) {
		case SCRIPT_LIBRARY:
			SingleTaskEntity taskEntity = singleTaskDao.selectById(dto.getTaskId());
			if (taskEntity.getScriptSrc().name().equals(dto.getScriptSrc().name())) {
				if(taskEntity.getScriptId() == scriptId) {
					taskEntity = taskSaveOrUpdate(dto, scriptId);// 未修改脚本
				}
			}else {
				taskEntity = singleTaskCreate(dto, scriptId);
			}
			return taskEntity;
			
		case USER_LOCAL_LIBRARY:
			return singleTaskCreate(dto, file);
			
		case USER_ONLINE_EDIT:
			return singleTaskCreate(dto, context, scriptType);
					
		default:
			return null;
		}
	}

	/**
	 * 单脚本任务执行
	 * 
	 * @param targetHostsDto
	 * @return
	 */
	public List<SingleTaskTargetEntity> singleTaskExec(TargetHostsDto targetHostsDto) {
		// 获取任务信息
		SingleTaskEntity scriptEntity = singleTaskDao.selectById(targetHostsDto.getSingleTaskId());
		// 执行任务
		List<SingleTaskTargetEntity> taskTargetList = new ArrayList<SingleTaskTargetEntity>();
		final BlockingDeque<Future<SingleTaskTargetEntity>> blockingDeque = new LinkedBlockingDeque<Future<SingleTaskTargetEntity>>(
				targetHostsDto.getTargetHosts().size());
		final CompletionService<SingleTaskTargetEntity> completionService = new ExecutorCompletionService<SingleTaskTargetEntity>(
				ThreadPoolUtil.getInstance(), blockingDeque);
		int i = 0;// 计数任务执行线程数
		for (HostDto host : targetHostsDto.getTargetHosts()) {
			// 准备ssh连接
			SSHAccount sshAccount = new SSHAccount(host.getAccount(), host.getPassword(), host.getHostIp());
			if (null != host.getPort() && host.getPort() != 22) {
				sshAccount.setPort(host.getPort());
			}
			InputStream fileStream = scriptServiceClient.tempRead(scriptEntity.getScriptId());
			String fileName = scriptServiceClient.temporaryScriptInfoById(scriptEntity.getScriptId()).getName();
			Session session = SSHSessionFactory.getSession(sshAccount);
			if (null == session) {
				// 获取ssh连接失败
				SingleTaskTargetEntity targetEntity = new SingleTaskTargetEntity(scriptEntity, host.getHostIp(),
						host.getPort(), host.getAccount(), host.getPassword(), Boolean.FALSE,
						ErrorMsg.SSH_CONNECT_FAILED.getMsg());
				taskTargetList.add(targetEntity);
				continue;
			}
			// 提交任务至线程池
			completionService.submit(new Callable<SingleTaskTargetEntity>() {

				@Override
				public SingleTaskTargetEntity call() throws Exception {
					// 上传脚本
					SingleTaskTargetEntity targetEntity = new SingleTaskTargetEntity(scriptEntity, host.getHostIp(),
							host.getPort(), host.getAccount(), host.getPassword(), Boolean.FALSE, null);
					OptResultDto sftp = sftpUpload(session, fileStream, fileName);
					if (!sftp.isSuccess()) {
						targetEntity.setResult(sftp.getResult());
						return targetEntity;
					}
					OptResultDto cmd = sshCmd(session, "./" + fileName);// 执行脚本
					targetEntity.setSuccess(Boolean.TRUE);
					targetEntity.setResult(cmd.getResult());
					sftpDelete(session, fileName);// 删除脚本
					return targetEntity;
				}
			});
			i++;

		}
		// 获取任务结果集
		for (int j = 1; j <= i; j++) {
			try {
				Future<SingleTaskTargetEntity> future = completionService.take();// 阻塞等待第一个结果，返回后该结果从队列删除
				taskTargetList.add(future.get());// get不会阻塞
			} catch (InterruptedException e) {
				LOGGER.error(e.getMessage());
			} catch (ExecutionException e) {
				LOGGER.error(e.getMessage());
			}
		}
		singleTaskTargetDao.delBySingleTaskId(scriptEntity.getId());// 删除上次执行任务结果
		singleTaskTargetDao.batchSave(taskTargetList);

		// 更新任务生命周期
		SingleTaskLifeEntity lifeEntity = scriptEntity.getTaskLife();
		lifeEntity.execNumAdd();
		lifeEntity.setExecDate(System.currentTimeMillis());
		lifeEntity.setExecutor(JWTUtil.getReqValue("realName"));
		singleTaskLifeDao.save(lifeEntity);
		return taskTargetList;

	}
	
	public List<SingleTaskEntity> singleTaskGet(SingleTaskQueryDto queryDto) {

		return singleTaskDao.selectByCondition(queryDto);
	}

	public List<SingleTaskTargetEntity> singleTaskTargetGet(int taskId) {

		return singleTaskTargetDao.selectBySingleTaskId(taskId);
	}

	/**
	 * shell单指令执行
	 * 
	 * @param cmd
	 * @return
	 * @throws IOException
	 * @throws JSchException
	 * @throws InterruptedException
	 */
	public OptResultDto sshCmd(Session session, String cmd) {
		SSHExecutor executor = null;
		String result = null;
		try {
			executor = new SSHExecutor(session);
			result = executor.exec(cmd);
			return OptResultDto.build(true, result);
		} catch (JSchException e) {
			LOGGER.error("获取ssh session失败", e);
			return OptResultDto.build(false, "ssh连接失败");
		} catch (IOException e) {
			LOGGER.error("文件流读取失败", e);
			return OptResultDto.build(false, "文件流读取失败");
		} catch (InterruptedException e) {
			LOGGER.error("执行线程中断", e);
			return OptResultDto.build(false, "执行线程中断");
		} finally {
			if (null != executor) {
				executor.disconnect();
			}
		}
	}

	/**
	 * 删除脚本
	 * 
	 * @param session
	 * @param fileName
	 */
	public void sftpDelete(Session session, String fileName) {
		SSHExecutor executor = null;
		try {
			executor = new SSHExecutor(session);
			executor.sftpRm(session, fileName);
		} catch (JSchException e) {
			LOGGER.error("获取ssh连接失败", e);
		} catch (SftpException e) {
			LOGGER.error("sftp操作失败", e);
		} finally {
			if (null != executor) {
				executor.disconnect();
			}
		}
	}

	/**
	 * 文件上传
	 * 
	 * @throws Exception
	 */
	public OptResultDto sftpUpload(Session session, InputStream fileStream, String fileName) {
		SSHExecutor executor = null;
		try {
			executor = new SSHExecutor(session);
			executor.sftpPut(session, fileName, fileStream);
			return OptResultDto.build(true, null);
		} catch (JSchException e) {
			LOGGER.error("ssh连接失败", e);
			return OptResultDto.build(false, "ssh连接失败");
		} catch (SftpException e) {
			LOGGER.error("ssh连接失败", e);
			return OptResultDto.build(false, "sftp执行失败");
		} catch (IOException e) {
			LOGGER.error("文件流读取失败", e);
			return OptResultDto.build(false, "文件流读取失败");
		} finally {
			if (null != executor) {
				executor.disconnect();
			}
		}

	}
}