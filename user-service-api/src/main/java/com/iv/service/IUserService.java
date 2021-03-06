package com.iv.service;


import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.iv.common.response.ResponseDto;
import com.iv.enter.dto.AccountDto;
import com.iv.enumeration.LoginType;
import com.iv.enter.dto.UsersQueryDto;
import com.iv.outer.dto.LocalAuthDto;
import com.iv.outer.dto.UserOauthDto;


/**
 * 用户与团队管理api
 * @author zhangying
 * 2018年4月4日
 * aggregation-1.4.0-SNAPSHOT
 */

public interface IUserService {
	
	/**
	 * 查询用户信息
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/user/info", method = RequestMethod.GET)
	ResponseDto getUserInfo(@RequestParam("request") HttpServletRequest request);
	
	/**
	 * 查看用户是否已绑定三方登录
	 * @param unionid
	 * @return
	 */
	@RequestMapping(value = "/permit/user/bindInfo", method = RequestMethod.GET)
	UserOauthDto bindInfo(@RequestParam("unionid") String unionid, @RequestParam("loginType") LoginType loginType);
	
	/**
	 * 用户绑定微信信息
	 * @param accountDto
	 * @return
	 */
	@RequestMapping(value = "/bind/wechatInfo", method = RequestMethod.POST)
	ResponseDto bindWechatInfo(@RequestBody AccountDto accountDto);
	
	/**
	 * 注册用户信息
	 * @param accountDto
	 * @return
	 */
	@RequestMapping(value = "/register/account", method = RequestMethod.POST)
	ResponseDto registerAccount(@RequestBody AccountDto accountDto);
	
	/**
	 * 根据id查询用户信息
	 * @param userId
	 * @return
	 * @throws RuntimeException
	 */
	@RequestMapping(value = "/permit/select/localAuthById",method = RequestMethod.GET)
	LocalAuthDto selectLocalAuthById(@RequestParam("userId") int userId);
	
	/**
	 * 根据用户id  登录方式  查询unionid
	 * @param userId
	 * @param loginType
	 * @return
	 * @throws RuntimeException
	 */
	@RequestMapping(value = "/select/UserWechat",method = RequestMethod.GET)
	UserOauthDto selectUserWechatUnionid(@RequestParam("userId") int userId, @RequestParam("loginType") LoginType loginType);
	
	/**
	 * 根据用户id集合、租户信息，查询用户信息（包含角色）
	 * @param usersWechatsQuery
	 * @return
	 */
	@RequestMapping(value = "/select/userInfos",method = RequestMethod.POST)
	List<LocalAuthDto> selectUserInfos(@RequestBody UsersQueryDto usersWechatsQuery,@RequestParam("tenantId") String tenantId);
	
	/**
	 * 根据用户id集合，查询用户信息
	 * @param usersWechatsQuery
	 * @return
	 */
	@RequestMapping(value = "/select/userDetails",method = RequestMethod.POST)
	List<LocalAuthDto> selectUserInfos(@RequestBody UsersQueryDto usersWechatsQuery);
	
	/**
	 * 根据用户名称查询用户信息
	 * @param userName
	 * @return
	 */
	@RequestMapping(value = "/select/localauthInfoByName",method = RequestMethod.GET)
	LocalAuthDto selectLocalauthInfoByName(@RequestParam("userName") String userName);
	
	/**
	 * 更改用户信息
	 * @param user
	 * @return
	 * @throws RuntimeException
	 */
	@RequestMapping(value = "/saveOrUpdate/localAuth",method = RequestMethod.POST)
	ResponseDto saveOrUpdateUserAuth(@RequestBody AccountDto accountDto);
	
	/**
	 * 找回用户密码
	 * @return
	 */
	@RequestMapping(value = "/find/localAuthPassWord",method = RequestMethod.POST)
	ResponseDto findLocalAuthPassWord(@RequestBody AccountDto accountDto);
	
	/**
	 * 根据用户列表id、登录方式查找联合主键
	 * @param UsersQueryDto
	 * @return
	 */
	@RequestMapping(value = "/select/usersUnionid",method = RequestMethod.POST)
	Set<String> selectUsersWechatUnionid(@RequestBody UsersQueryDto UsersQueryDto);
	
	
}
