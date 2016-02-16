package com.ouyang.memcached.autoconfigure;

import com.ouyang.memcached.interceptor.MemcacheInterceptor;
import com.ouyang.memcached.interceptor.MemcacheUpdateInterceptor;
import com.ouyang.xmemcached.XMemcachedClient;
import com.ouyang.xmemcached.command.BinaryCommandFactory;
import com.ouyang.xmemcached.impl.KetamaMemcachedSessionLocator;
import com.ouyang.xmemcached.utils.XMemcachedClientFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wq
 * @since 2015-11-24
 */
@Configuration
public class MemcacheClientConfig {

    @Value("${memcached.servers:127.0.0.1:11211}")
    private String servers;

    @Bean(name = "commonMemcachedClient")
    public XMemcachedClient commonMemcachedClient() throws Exception {
        XMemcachedClientFactoryBean xMemcachedClientFactoryBean = new XMemcachedClientFactoryBean();
        xMemcachedClientFactoryBean
                .setServers(servers);
        xMemcachedClientFactoryBean.setConnectionPoolSize(1);
        xMemcachedClientFactoryBean.setSessionLocator(new KetamaMemcachedSessionLocator());
        xMemcachedClientFactoryBean.setCommandFactory(new BinaryCommandFactory());
        return (XMemcachedClient) xMemcachedClientFactoryBean.getObject();
    }

    @Bean
    public MemcacheInterceptor commonMemcacheInterceptor() {
        return new MemcacheInterceptor();
    }

    @Bean
    public MemcacheUpdateInterceptor commonMemcacheUpdateInterceptor() {
        return new MemcacheUpdateInterceptor();
    }

    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }
}
