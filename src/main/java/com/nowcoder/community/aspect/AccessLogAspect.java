package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

//@Component
//@Aspect
public class AccessLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(AccessLogAspect.class);

    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
//    @Pointcut("@annotation(com.nowcoder.community.aspect.LogPointCut)")
    public void pt(){}

    @Around("pt()")
    public Object printAccessLog(ProceedingJoinPoint pjp) {
        Object ret = null;
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            String url = request.getRemoteHost();
            String now = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date());
            String className = pjp.getSignature().getDeclaringTypeName();
            String methodName = pjp.getSignature().getName();
            logger.info("用户{}在{}访问了{}的{}", url, now, className, methodName);
            ret = pjp.proceed();
        } catch (Throwable throwable) {
            logger.error("记录用户访问日志时出错");
        }
        return ret;
    }
}
