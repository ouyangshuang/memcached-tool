package com.ouyang.memcached.interceptor;

import com.alibaba.fastjson.JSON;
import com.ouyang.memcached.annotation.MemcacheNamespaceKey;
import com.ouyang.memcached.MD5;
import com.ouyang.memcached.annotation.Memcache;
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
 * Memcache 注解处理器 默认30分钟有效 不更新
 * MemcachePrimaryKey 0-1个 暂时都只支持一个 主键对象，单个对象，查询 会覆盖 MemcacheNamespaceKey
 * MemcacheNamespaceKey 批量更新   0-1个暂时都只支持一个
 * <p>
 * 命名空间 xmemcached 本身支持但是命名空间存储在本地，不适合多节点的情况
 * 另外 memcache缓存的对象序列化id发生变化后会报错
 *
 * @author ouyang
 * @since 2015-03-23 12:14
 */
@Aspect
public class MemcacheInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemcacheInterceptor.class);

    @Autowired
    private XMemcachedClient commonMemcachedClient;

    /**
     * 拦截 @Memcache  aop
     *
     * @author ouyang
     * @since 2015-04-24 10:22
     */
    @SuppressWarnings("EmptyMethod")
    @Pointcut("@annotation(com.ouyang.memcached.annotation.Memcache)")
    private void commonMemcachePointCut() {
    }

    /**
     * 处理拦截 @Memcache  aop 的方法 查询缓存
     *
     * @param joinPoint 代理点 切点
     * @return 返回代理方法的返回值
     * @throws Throwable
     * @author ouyang
     * @since 2015-04-24 10:24
     */
    @Around("commonMemcachePointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        try {
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;

            //切面的方法参数
            Object[] args = joinPoint.getArgs();

            //paramsAnoAry二维数组， 每一个paramsAnoAry[i] 可能有多个注解
            //MemcacheNamespaceKey 0-N个
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
                            if (args[i] != null) {
                                commonMemcacheNamespaceKeyArgList.add(args[i].toString());
                            }
                        }
                        if (annotation instanceof MemcachePrimaryKey) {
                            if (args[i] != null) {
                                commonMemcachePrimaryKeyArgList.add(args[i].toString());
                            }
                        }
                    }
                }
            }
            Memcache memcache = methodSignature.getMethod().getAnnotation(Memcache.class);
            int exp = memcache.exp();
            //非生产环境默认缓存为1分钟
//            if(!"production".equals(ResourceMap.getEnv()) && exp == 30){
//                exp = 1;
//            }

            //数据操作层class 一般统一为 DAO
            String daoClassName = joinPoint.getTarget().getClass().getName();
            String methodName = methodSignature.getMethod().getName();
            String returnName = methodSignature.getMethod().getReturnType().getName();

            //主键优先使用
            if (commonMemcachePrimaryKeyArgList.size() > 0) {
                if (commonMemcacheNamespaceKeyArgList.size() > 0) {
                    LOGGER.error("MemcachePrimaryKey 主键对象，单个对象，查询 会覆盖 MemcacheNamespaceKey");
                }
                //主键加上当前dao空间
                String memcachedKey = daoClassName + commonMemcachePrimaryKeyArgList.get(0);

                //memcachedKey的字节长度不能超过250个，所以需要转码
                //超过250长度的用md5转码
                if (ByteUtils.getBytes(memcachedKey).length > 250) {
                    memcachedKey = MD5.getMD5String(memcachedKey.getBytes());
                }

                Object returnObject = commonMemcachedClient.get(memcachedKey, 1000L);

                //object 查询为 null 返回 null 缓存放入对象 “null”
                if ("null".equals(returnObject)) {
                    return null;
                }
                if (returnObject == null) {
                    returnObject = joinPoint.proceed();
                    if (returnObject == null) {
                        commonMemcachedClient.set(memcachedKey, 60 * exp, "null");
                    } else {
                        commonMemcachedClient.set(memcachedKey, 60 * exp, returnObject);
                    }
                }
                return returnObject;
            } else if (commonMemcacheNamespaceKeyArgList.size() > 0) {
                //批量 用命名空间方式来实现  commonMemcachePrimaryKey 会覆盖 commonMemcacheNamespaceKey
                //主键加上当前dao空间
                String namespaceKey = daoClassName + commonMemcacheNamespaceKeyArgList.get(0);
                //memcachedKey的字节长度不能超过250个，所以需要转码
                //超过250长度的用md5转码
                if (ByteUtils.getBytes(namespaceKey).length > 250) {
                    namespaceKey = MD5.getMD5String(namespaceKey.getBytes());
                }
                String namespace = commonMemcachedClient.get(namespaceKey, 1000L);
                if (namespace == null) {
                    namespace = String.valueOf(System.nanoTime());
                    commonMemcachedClient.set(namespaceKey, 60 * exp, namespace);
                }
                String memcachedKey = namespace + '_' +
                                      daoClassName + '_' +
                                      methodName + '_' +
                                      JSON.toJSONString(methodSignature.getParameterTypes()) +
                                      JSON.toJSONString(joinPoint.getArgs()) +
                                      returnName;

                //memcachedKey的字节长度不能超过250个，所以需要转码
                //超过250长度的用md5转码
                if (ByteUtils.getBytes(memcachedKey).length > 250) {
                    memcachedKey = MD5.getMD5String(memcachedKey.getBytes());
                }

                Object returnObject = commonMemcachedClient.get(memcachedKey, 1000L);

                //object 查询为 null 返回 null 缓存放入对象 “null”
                if ("null".equals(returnObject)) {
                    return null;
                }
                if (returnObject == null) {
                    returnObject = joinPoint.proceed();
                    if (returnObject == null) {
                        commonMemcachedClient.set(memcachedKey, 60 * exp, "null");
                    } else {
                        commonMemcachedClient.set(memcachedKey, 60 * exp, returnObject);
                    }
                }
                return returnObject;
            } else {
                String memcachedKey = daoClassName + '_' +
                                      methodName + '_' +
                                      JSON.toJSONString(methodSignature.getParameterTypes()) +
                                      JSON.toJSONString(joinPoint.getArgs()) +
                                      returnName;

                if (ByteUtils.getBytes(memcachedKey).length > 250) {
                    memcachedKey = MD5.getMD5String(memcachedKey.getBytes());
                }

                Object returnObject = commonMemcachedClient.get(memcachedKey, 1000L);

                //object 查询为 null 返回 null 缓存放入对象 “null”
                if ("null".equals(returnObject)) {
                    return null;
                }
                if (returnObject == null) {
                    returnObject = joinPoint.proceed();
                    if (returnObject == null) {
                        commonMemcachedClient.set(memcachedKey, 60 * exp, "null");
                    } else {
                        commonMemcachedClient.set(memcachedKey, 60 * exp, returnObject);
                    }
                }
                return returnObject;
            }
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            //如果出错直接执行原来的方法
            LOGGER.error(e.getClass().getName(), e);
            return joinPoint.proceed();
        }
    }

}
