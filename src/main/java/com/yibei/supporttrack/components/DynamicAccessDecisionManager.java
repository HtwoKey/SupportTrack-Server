package com.yibei.supporttrack.components;

import com.yibei.supporttrack.entity.bo.SystemUserDetails;
import com.yibei.supporttrack.entity.po.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Collection;
import java.util.function.Supplier;

@Slf4j
@Component
public class DynamicAccessDecisionManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (auth == null) {
            return new AuthorizationDecision(false);
        }

        if (isSuperAdmin(auth)) {
            return new AuthorizationDecision(true);
        }

        return hasPermission(auth.getAuthorities(), getRequestUri(context));
    }

    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationManager.super.verify(authentication, object);
    }

    private boolean isSuperAdmin(Authentication auth) {
        Object principal = auth.getPrincipal();

        // 类型安全校验
        if (!(principal instanceof SystemUserDetails user)) {
            log.warn("认证主体类型不匹配，期望类型: User，实际类型: {}",
                    (principal != null ? principal.getClass().getName() : "null"));
            return false;
        }
        return Boolean.TRUE.equals(user.isAdmin());
    }

    private AuthorizationDecision hasPermission(Collection<? extends GrantedAuthority> authorities, String requestUri) {
        if (authorities.isEmpty()) {
            return new AuthorizationDecision(false);
        }

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(permission -> pathMatcher.match(permission, requestUri))
                ? new AuthorizationDecision(true)
                : new AuthorizationDecision(false);
    }

    private String getRequestUri(RequestAuthorizationContext context) {
        return context.getRequest().getRequestURI();
    }
}
