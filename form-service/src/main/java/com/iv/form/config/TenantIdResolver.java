package com.iv.form.config;

import com.iv.common.util.spring.ConstantContainer;
import com.iv.common.util.spring.JWTUtil;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
public class TenantIdResolver implements CurrentTenantIdentifierResolver {

	@Override
	public String resolveCurrentTenantIdentifier() {

		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if(null == requestAttributes) {
			return ConstantContainer.ALARM_AGGREGATION_DB;
		}
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
		String token = request.getHeader("Authorization");

		if (StringUtils.isEmpty(token)) {
			return ConstantContainer.ALARM_AGGREGATION_DB;
		} else {
			return JWTUtil.getJWtJson(token).getString("curTenantId");
		}
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}

}
