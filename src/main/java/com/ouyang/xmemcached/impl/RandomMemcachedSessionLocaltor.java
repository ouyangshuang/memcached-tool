package com.ouyang.xmemcached.impl;

import com.ouyang.code.yanf4j.core.Session;
import com.ouyang.xmemcached.MemcachedSessionLocator;

import java.util.*;

/**
 * A random session locator,it can be used in kestrel.
 *
 * @author dennis<killme2008@gmail.com>
 */
public class RandomMemcachedSessionLocaltor implements MemcachedSessionLocator {
    private final Random rand = new Random();
    private transient volatile List<Session> sessions = Collections.emptyList();

    public Session getSessionByKey(String key) {
        List<Session> copiedOnWrite = sessions;
        if (copiedOnWrite == null || copiedOnWrite.isEmpty())
            return null;
        return copiedOnWrite.get(rand.nextInt(copiedOnWrite.size()));
    }

    public void updateSessions(Collection<Session> list) {
        this.sessions = new ArrayList<Session>(list);

    }

    public void setFailureMode(boolean failureMode) {
        // ignore
    }

}
