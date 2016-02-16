package com.ouyang.memcached.interceptor;

import com.ouyang.memcached.annotation.MemcacheNamespaceKey;
import com.ouyang.memcached.MD5;
import com.ouyang.memcached.annotation.MemcachePrimaryKey;
import com.ouyang.xmemcached.XMemcachedClient;
import com.ouyang.xmemcached.exception.MemcachedException;
import com.ouyang.xmemcached.utils.ByteUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * MemcacheUpdate 缓存更新
 * 分为单条和批量更新的方式
 * MemcachePrimaryKey 单条更新    暂时都只支持一个
 * MemcacheNamespaceKey 批量更新   暂时都只支持一个
 *
 * @author ouyang
 * @since 2015-03-23 12:14
 */
@Aspect
public class MemcacheUpdateInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemcacheUpdateInterceptor.class);
    @Autowired
    private XMemcachedClient commonMemcachedClient;

    /**
     * 拦截 @MemcacheUpdate  aop
     *
     * @author ouyang
     * @since 2015-04-24 10:24
     */
    @SuppressWarnings("EmptyMethod")
    @Pointcut("@annotation(com.ouyang.memcached.annotation.MemcacheUpdate)")
    private void commonMemcacheUpdatePointCut() {
    }

    /**
     * 处理拦截 @MemcacheUpdate  aop
     *
     * @param joinPoint 代理点
     * @return 代理方法的返回值
     * @throws Throwable
     * @author ouyang
     * @since 2015-04-24 10:24
     */
    @Around("commonMemcacheUpdatePointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object returnObject = joinPoint.proceed();
        try {
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;

            //切面的方法参数
            Object[] args = joinPoint.getArgs();

            //paramsAnoAry二维数组， 每一个paramsAnoAry[i] 可能有多个注解
            //MemcacheNamespaceKey 0-1个
            List<String> commonMemcacheNamespaceKeyArgList = new ArrayList<>();
            //MemcachePrimaryKey  0-1个
            List<String> commonMemcachePrimaryKeyArgList = new ArrayList<>();

            Annotation[][] paramsAnoAry = methodSignature.getMethod().getParameterAnnotations();
            //This inspection is intended for J2ME and other highly resource constrained environments.
            // Applying the results of this inspection without consideration might have negative effects on code clarity and design.
            //Reports any access to the .length of an array in the condition part of a loop statement.
            // In highly resource constrained environments, such calls may have adverse performance implications.
            int paaLength = paramsAnoAry.length;
            for (int i = 0; i < paaLength; i++) {
                Annotation[] annotationAry = paramsAnoAry[i];
                if (annotationAry.length > 0) {
                    for (Annotation annotation : annotationAry) {
                        if (annotation instanceof MemcacheNamespaceKey) {
                            commonMemcacheNamespaceKeyArgList.add(args[i].toString());
                        }
                        if (annotation instanceof MemcachePrimaryKey) {
                            commonMemcachePrimaryKeyArgList.add(args[i].toString());
                        }
                    }
                }
            }

            //数据操作层class 一般统一为 DAO
            String daoClassName = joinPoint.getTarget().getClass().getName();

            //单条
            if (commonMemcachePrimaryKeyArgList.size() > 0) {
                //主键加上当前dao空间
                String memcachedKey = daoClassName + commonMemcachePrimaryKeyArgList.get(0);
                //memcachedKey的字节长度不能超过250个，所以需要转码
                //超过250长度的用md5转码
                if (ByteUtils.getBytes(memcachedKey).length > 250) {
                    memcachedKey = MD5.getMD5String(memcachedKey.getBytes());
                }
                commonMemcachedClient.delete(memcachedKey);
            }
            //批量 用命名空间方式来实现
            if (commonMemcacheNamespaceKeyArgList.size() > 0) {
                //主键加上当前dao空间
                String namespaceKey = daoClassName + commonMemcacheNamespaceKeyArgList.get(0);
                //memcachedKey的字节长度不能超过250个，所以需要转码
                //超过250长度的用md5转码
                if (ByteUtils.getBytes(namespaceKey).length > 250) {
                    namespaceKey = MD5.getMD5String(namespaceKey.getBytes());
                }
                commonMemcachedClient.delete(namespaceKey);
            }
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            LOGGER.error(e.getClass().getName(), e);
        }
        return returnObject;
    }

}




