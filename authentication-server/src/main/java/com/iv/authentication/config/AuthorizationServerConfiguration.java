package com.iv.authentication.config;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import com.iv.authentication.pojo.LocalUser;
import com.iv.authentication.service.MyUserDetailsService;
import com.iv.common.util.spring.Constants;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter{
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private MyUserDetailsService userDetailsService;
	
	/**
	 * 用来配置令牌端点(Token Endpoint)的安全约束
	 */
	@Override
	public void configure(final AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
	}
	
	/**
	 * 用来配置客户端详情服务（ClientDetailsService），
	 * 客户端详情信息在这里进行初始化，你能够把客户端详情信息写死在这里或者是通过数据库来存储调取详情信息
	 */
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory() // 使用in-memory存储 
		.withClient(Constants.OAUTH2_CLIENT_ID) // client_id 
		.secret(Constants.OAUTH2_CLIENT_SECRET) // client_secret 
		.authorizedGrantTypes("password", "refresh_token") // 该client允许的授权类型
		//.accessTokenValiditySeconds(3600)
		//.refreshTokenValiditySeconds(86400)
		.scopes("read", "write")//允许的授权范围xin
		.autoApprove(true); 
		/*clients.inMemory()  
        .withClient("client")//客户端ID  
        .authorizedGrantTypes("password", "refresh_token")//设置验证方式  
        .scopes("read", "write")  
        .secret("secret");*/
	}
	
	/**
	 * 进行授权的服务，Default URL: /oauth/authorize
	 */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.authenticationManager(authenticationManager)//直接注入一个AuthenticationManager，自动开启密码授权类型
		.userDetailsService(userDetailsService).accessTokenConverter(accessTokenConverter());//启动刷新token授权类型，会判断用户是否还是存活的
	}
	
	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter() {
			@Override
			public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
				//String userName = authentication.getUserAuthentication().getName();
				LocalUser user = (LocalUser) authentication.getPrincipal();
				final Map<String, Object> additionalInformation = new HashMap<String, Object>();
				//additionalInformation.put("user_name", userName);
				additionalInformation.put("userId", user.getUserId());
				additionalInformation.put("userName", user.getUsername());
				additionalInformation.put("curTenantId", user.getCurTenantId());
				additionalInformation.put("realName", user.getRealName());
				additionalInformation.put("email", user.getEmail());
				additionalInformation.put("tel", user.getTel());
				
				((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInformation);
				OAuth2AccessToken token = super.enhance(accessToken, authentication);
				return token;
			}
		};
		KeyPair keyPair = new KeyStoreKeyFactory(new ClassPathResource("kevin_key.jks"), "123456".toCharArray())
				.getKeyPair("kevin_key");
		converter.setKeyPair(keyPair);
		return converter;
	}
	
}
