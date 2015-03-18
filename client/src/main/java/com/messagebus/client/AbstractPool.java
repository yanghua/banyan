package com.messagebus.client;

import com.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * the abstract pool
 *
 * @param <T> the Object that the pool want to cache
 */
abstract class AbstractPool<T> {

    private static final Log logger = LogFactory.getLog(AbstractPool.class);

    protected GenericObjectPool<T> internalPool;

    public AbstractPool(final GenericObjectPoolConfig poolConfig,
                        PooledObjectFactory<T> factory) {
        this.initPool(poolConfig, factory);
    }

    private void initPool(final GenericObjectPoolConfig poolConfig,
                          PooledObjectFactory<T> factory) {
        if (this.internalPool != null) {
            closeInternalPool();
        }

        this.internalPool = new GenericObjectPool<T>(factory, poolConfig);
    }

    public T getResource() {
        try {
            return internalPool.borrowObject();
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "[getResource]");
            throw new RuntimeException("can not get a resource from the pool ", e);
        }
    }

    public void returnResourceObject(final T resource) {
        internalPool.returnObject(resource);
    }

    public void returnBrokenResource(final T resource) {
        returnBrokenResourceObject(resource);
    }

    public void returnResource(final T resource) {
        returnResourceObject(resource);
    }

    public void destroy() {
        closeInternalPool();
    }

    protected void returnBrokenResourceObject(final T resource) {
        try {
            internalPool.invalidateObject(resource);
        } catch (Exception e) {
            throw new RuntimeException("Could not return the resource to the pool", e);
        }
    }

    protected void closeInternalPool() {
        internalPool.close();
    }

}
