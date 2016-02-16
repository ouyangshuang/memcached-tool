package com.ouyang.memcached.autoconfigure;

import com.ouyang.xmemcached.XMemcachedClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author ouyang
 * @since 2015-11-27 16:12
 */
@Configuration
@ConditionalOnClass(XMemcachedClient.class)
@Import({MemcacheClientConfig.class})
public class MemcacheAutoConfiguration {
}
