package com.freedom.messagebus.client.core.pool;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jetbrains.annotations.NotNull;

/**
 * the abstract pool
 *
 * @param <T> the Object that the pool want to cache
 */
public abstract class AbstractPool<T> {

    protected GenericObjectPool<T> internalPool;

    public AbstractPool(@NotNull final GenericObjectPoolConfig poolConfig,
                        @NotNull PooledObjectFactory<T> factory) {
        this.initPool(poolConfig, factory);
    }

    private void initPool(@NotNull final GenericObjectPoolConfig poolConfig,
                          @NotNull PooledObjectFactory<T> factory) {
        if (this.internalPool != null) {
            closeInternalPool();
        }

        this.internalPool = new GenericObjectPool<T>(factory, poolConfig);
    }

    public T getResource() {
        try {
            return internalPool.borrowObject();
        } catch (Exception e) {
            throw new ChannelConnectException("can not get a resource from the pool ", e);
        }
    }

    public void returnResourceObject(@NotNull final T resource) {
        internalPool.returnObject(resource);
    }

    public void returnBrokenResource(@NotNull final T resource) {
        returnBrokenResourceObject(resource);
    }

    public void returnResource(@NotNull final T resource) {
        returnResourceObject(resource);
    }

    public void destroy() {
        closeInternalPool();
    }

    protected void returnBrokenResourceObject(@NotNull final T resource) {
        try {
            internalPool.invalidateObject(resource);
        } catch (Exception e) {
            throw new ChannelException("Could not return the resource to the pool", e);
        }
    }

    protected void closeInternalPool() {
        internalPool.close();
    }

}
