package com.iv.enter.dto;

import java.util.List;

/**
 * 根据userIds批量查询用户信息(包含微信头像信息)
 * @author zhangying
 * 2018年4月18日
 * aggregation-1.4.0-SNAPSHOT
 */
public class UsersWechatsQuery {
	private List<Integer> userIds;
	private String loginType;
	
	public List<Integer> getUserIds() {
		return userIds;
	}
	public void setUserIds(List<Integer> userIds) {
		this.userIds = userIds;
	}
	public String getLoginType() {
		return loginType;
	}
	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}
	
}
