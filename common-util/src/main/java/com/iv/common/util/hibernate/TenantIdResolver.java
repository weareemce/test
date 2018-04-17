package com.iv.common.util.hibernate;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.iv.common.util.spring.ConstantContainer;

@Component
public class TenantIdResolver implements CurrentTenantIdentifierResolver {

	@Override
	public String resolveCurrentTenantIdentifier() {
		
		Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
		/*if(null != authentication && authentication.getPrincipal() instanceof LocalAuthDetails) {
			
			// 登录用户，从数据库中获取当前的租户标识符
			LocalAuthDetails localAuthDetails = (LocalAuthDetails) authentication.getPrincipal();
			return localAuthDetails.getCurTenantId() != null ? localAuthDetails.getCurTenantId() : ConstantContainer.TENANT_SHARED_ID;
		
		} else {*/
			
			return ConstantContainer.TENANT_SHARED_ID;
		//}
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		// TODO Auto-generated method stub
		return true;
	}

}