package com.yibei.supporttrack.config;

import com.yibei.supporttrack.components.DynamicAccessDecisionManager;
import com.yibei.supporttrack.components.JwtAuthenticationTokenFilter;
import com.yibei.supporttrack.components.RestAuthenticationEntryPoint;
import com.yibei.supporttrack.components.RestfulAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfigure{

    // 引入自定义的JWT过滤器
    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    // 引入自定义的动态权限处理器
    private final DynamicAccessDecisionManager dynamicAccessDecisionManager;
    // 引入自定义的异常处理器
    private final RestfulAccessDeniedHandler restfulAccessDeniedHandler;
    // 引入自定义的认证处理器
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final AuthenticationConfiguration authenticationConfiguration;

    public SecurityConfigure( JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter, DynamicAccessDecisionManager dynamicAccessDecisionManager, RestfulAccessDeniedHandler restfulAccessDeniedHandler, RestAuthenticationEntryPoint restAuthenticationEntryPoint, AuthenticationConfiguration authenticationConfiguration) {
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
        this.dynamicAccessDecisionManager = dynamicAccessDecisionManager;
        this.restfulAccessDeniedHandler = restfulAccessDeniedHandler;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.authenticationConfiguration = authenticationConfiguration;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        security
                // 禁用表单登录和退出
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                // 配置请求权限
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // 限定所有路径的OPTIONS
                        .requestMatchers("/login", "/logout").permitAll()
                        .anyRequest().access(dynamicAccessDecisionManager))
                // 安全特性配置
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                // 会话管理
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 异常处理
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(restfulAccessDeniedHandler)
                        .authenticationEntryPoint(restAuthenticationEntryPoint))
                // 添加自定义过滤器
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                // 启用官方CORS配置代替自定义过滤器
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return security.build();
    }




    // 正确实现AuthenticationManager Bean
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 密码加密方式
     * @return PasswordEncoder
     */

    // 提取CORS配置到独立方法
    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 生产环境应替换为具体域名
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
