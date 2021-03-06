package com.iv.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.iv.common.response.ResponseDto;
import com.iv.dto.AlarmInfoTemplate;
import com.iv.dto.ErrorMsg;
import com.iv.dto.FormInfoTemplate;
import com.iv.service.EmailService;
import com.iv.service.IEmailService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(description = "邮件管理相关接口")
public class IEmailController implements IEmailService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IEmailController.class);
	@Autowired
	private EmailService emailService;	

	@Override
	@ApiOperation(value = "发送邮箱验证码", notes = "82500")
	public ResponseDto emailVCode(String email) {
		// TODO Auto-generated method stub
		ResponseDto dto = new ResponseDto();
		try {
			emailService.emailVCode(email);
			dto.setErrorMsg(com.iv.common.response.ErrorMsg.OK);
			return dto;
		} catch (Exception e) {
			LOGGER.error("系统错误：邮箱验证码发送失败", e);
			dto.setErrorMsg(ErrorMsg.EMAIL_VCODE_SEND_FAILED);
			return dto;
		}
	}

	@Override
	@ApiOperation(value = "邮箱发送模板消息", notes = "82501")
	public ResponseDto alarmToMail(@RequestBody AlarmInfoTemplate alarmInfoTemplate) {
		// TODO Auto-generated method stub		
		try {
			emailService.sendToMail(alarmInfoTemplate);
			return ResponseDto.builder(ErrorMsg.OK);
		} catch (Exception e) {
			LOGGER.error("系统错误：邮箱发送模板消息失败", e);
			return ResponseDto.builder(ErrorMsg.EMAIL_SEND_INFo_FAILED);
		}
	}

	@Override
	@ApiOperation(value = "邮箱发送工单消息", notes = "82502")
	public ResponseDto formToMail(@RequestBody FormInfoTemplate formInfoTemplate) {
		// TODO Auto-generated method stub
		try {
			emailService.sendFormInfoToMail(formInfoTemplate);
			return ResponseDto.builder(ErrorMsg.OK);
		} catch (Exception e) {
			LOGGER.error("系统错误：邮箱发送工单模板消息失败", e);
			return ResponseDto.builder(ErrorMsg.EMAIL_SEND_INFo_FAILED);
		}
	}

}
