package com.iv.service;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.iv.common.enumeration.SendType;
import com.iv.common.response.ResponseDto;
import com.iv.dto.AlarmInfoTemplate;
import com.iv.dto.FormInfoTemplate;


public interface IEmailService {

	/**
	 * 发送邮箱验证码
	 * @param email
	 * @return
	 */
	@RequestMapping(value = "/register/vcode", method = RequestMethod.GET)
	ResponseDto emailVCode(@RequestParam("email") String email);
	
	/**
	 * 邮箱发送告警信息
	 */
	@RequestMapping(value = "/send/templateMessage", method = RequestMethod.POST)
	ResponseDto alarmToMail(@RequestBody AlarmInfoTemplate alarmInfoTemplate);
	
	/**
	 * 邮箱发送工单消息
	 * @param alarmInfoTemplate
	 * @return
	 */
	@RequestMapping(value = "/send/formMessage", method = RequestMethod.POST)
	ResponseDto formToMail(@RequestBody FormInfoTemplate formInfoTemplate);
}
