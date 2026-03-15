package com.aerobook.aspect;

import com.aerobook.annotations.ExemptAuthorization;
import com.aerobook.exception.AeroBookException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * The type Controller security aspect.
 */
@Slf4j
@Aspect
@Component
public class ControllerSecurityAspect {

    /**
     * Rest controller methods.
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {
    }

    /**
     * Enforce access check for every method.
     *
     * @param joinPoint the join point
     * @return the object
     * @throws Throwable the throwable
     */
    @Around("restControllerMethods()")
    public Object enforceAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        String httpMethod = currentRequest().getMethod().toUpperCase();
        String uri = currentRequest().getRequestURI();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        log.debug("ControllerSecurityAspect — {} {}", httpMethod, uri);

        if (isExempt(method)) {
            return handleExemptAccess(joinPoint, method, uri);
        }

        validatePreAuthorizePresent(method, httpMethod, uri);

        validateAuthenticated(httpMethod, uri);

        log.debug("Access granted — proceeding: {}", uri);
        return joinPoint.proceed();
    }

    /**
     * Method to handle Exempt Authorization
     *
     * @param joinPoint jointPoint
     * @param method    Method
     * @param uri       uri
     * @return Object
     * @throws Throwable throwable
     */
    private Object handleExemptAccess(ProceedingJoinPoint joinPoint,
                                      Method method,
                                      String uri) throws Throwable {
        String reason = method.getAnnotation(ExemptAuthorization.class).reason();
        log.debug("EXEMPT — bypassing auth check: {} | reason: {}",
                uri, reason.isBlank() ? "not specified" : reason);
        return joinPoint.proceed();
    }


    /**
     * Method to validate presence of PreAuthorize
     *
     * @param method     method
     * @param httpMethod httpMethod
     * @param uri        uri
     */
    private void validatePreAuthorizePresent(Method method,
                                             String httpMethod,
                                             String uri) {
        if (!hasPreAuthorize(method)) {
            log.warn("BLOCKED — CUD endpoint missing @PreAuthorize: {} {}", httpMethod, uri);
            throw new AeroBookException(
                    "Access denied — endpoint not configured for access",
                    HttpStatus.FORBIDDEN,
                    "ENDPOINT_NOT_AUTHORIZED"
            );
        }
    }

    /**
     * Method to Validate Authentication
     *
     * @param httpMethod httpMethod
     * @param uri        uri
     */
    private void validateAuthenticated(String httpMethod, String uri) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isNotAuthenticated(auth)) {
            log.warn("BLOCKED — unauthenticated CUD request: {} {}", httpMethod, uri);
            throw new AeroBookException(
                    "Access denied — authentication required",
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED"
            );
        }
    }

    private boolean isExempt(Method method) {
        return method.isAnnotationPresent(ExemptAuthorization.class);
    }


    private boolean hasPreAuthorize(Method method) {
        return method.isAnnotationPresent(PreAuthorize.class)
                || method.getDeclaringClass().isAnnotationPresent(PreAuthorize.class);
    }

    private boolean isNotAuthenticated(Authentication auth) {
        return auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal());
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new AeroBookException(
                    "No HTTP request context available",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "NO_REQUEST_CONTEXT"
            );
        }
        return attributes.getRequest();
    }
}
