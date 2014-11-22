package com.freedom.messagebus.business.exchanger;

import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.interactor.zookeeper.IConfigChangedListener;
import com.freedom.messagebus.interactor.zookeeper.LongLiveZookeeper;
import com.freedom.messagebus.interactor.zookeeper.ZKEventType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ExchangerManager {

    private static final Log logger = LogFactory.getLog(ExchangerManager.class);

    private List<String>                paths;
    private List<IDataExchanger>        exchangers;
    private Map<String, IDataExchanger> pathExchangeMap;
    private Map<String, String>         tablePathMap;
    private Map<String, IDataFetcher>   tableDataFetcherMap;

    private boolean dataFetcherInited = false;
    private Map<String, List<IExchangerListener>> registry;
    private LongLiveZookeeper                     zookeeper;

    private static volatile ExchangerManager instance = null;

    private ExchangerManager(String zkHost, int zkPort) {
        this.zookeeper = new LongLiveZookeeper(zkHost, zkPort);
        this.zookeeper.open();

        boolean isZKAlive = this.zookeeper.isAlive();
        if (!isZKAlive)
            return;

        scan();

        //add external path
        this.paths.add(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_AUTH);

        registry = new ConcurrentHashMap<>();

        watchZookeeper();
    }

    public boolean isZKAlive() {
        return this.zookeeper.isAlive();
    }

    public void retryConnect(String zkHost, int zkPort) {
        if (this.zookeeper != null && this.zookeeper.isAlive())
            this.destroy();

        this.zookeeper = null;
        this.zookeeper = new LongLiveZookeeper(zkHost, zkPort);
        this.zookeeper.open();
    }

    public static ExchangerManager defaultExchangerManager(String zkHost, int zkPort) {
        if (instance == null) {
            synchronized (ExchangerManager.class) {
                if (instance == null) {
                    instance = new ExchangerManager(zkHost, zkPort);
                }
            }
        }

        return instance;
    }

    public synchronized void uploadAll() {
        this.beforeUploadThroughFetcher();

        try {
            for (IDataExchanger exchanger : this.exchangers) {
                exchanger.upload();
            }
        } catch (IOException e) {
            logger.error("[uploadAll] occurs a IOException : " + e.getMessage());
        }
    }

    public synchronized void uploadWithPath(String path) throws IOException {
        this.beforeUploadThroughFetcher();

        if (!this.pathExchangeMap.containsKey(path)) {
            throw new RuntimeException("[uploadWithPath] the path : " + path);
        }

        IDataExchanger exchanger = this.pathExchangeMap.get(path);
        exchanger.upload();
    }

    public synchronized void uploadWithTable(String tbName) throws IOException {
        this.beforeUploadThroughFetcher();

        if (!this.tablePathMap.containsKey(tbName))
            throw new RuntimeException("[uploadWithTable] the table name : " + tbName);

        String path = this.tablePathMap.get(tbName);
        this.uploadWithPath(path);
    }

    public synchronized void uploadWithPath(String path, Serializable data) throws IOException {
        if (!pathExchangeMap.containsKey(path)) {
            logger.error("[uploadWithPath] illegal path : " + path);
            throw new RuntimeException("illegal path : " + path);
        }

        IDataExchanger exchanger = pathExchangeMap.get(path);
        exchanger.upload(data);
    }

    public synchronized void uploadWithTable(String tbName, Serializable data) throws IOException {
        if (!tablePathMap.containsKey(tbName)) {
            logger.error("[uploadWithTable] illegal table name : " + tbName);
            throw new RuntimeException("illegal table name : " + tbName);
        }

        String path = tablePathMap.get(tbName);

        pathExchangeMap.get(path).upload(data);
    }

    public synchronized Object downloadWithPath(String path) throws IOException {
        if (!pathExchangeMap.containsKey(path)) {
            logger.error("[downloadWithPath] illegal path : " + path);
            throw new RuntimeException("illegal path : " + path);
        }

        return pathExchangeMap.get(path).download();
    }

    public synchronized Object downloadWithTable(String tbName) throws IOException {
        if (!tablePathMap.containsKey(tbName)) {
            logger.error("[downloadWithTable] illegal table name : " + tbName);
            throw new RuntimeException("illegal table name : " + tbName);
        }

        String path = tablePathMap.get(tbName);

        return this.downloadWithPath(path);
    }

    public synchronized void registerWithPath(String path, IExchangerListener onChanged) {
        List<IExchangerListener> listenersOfPath = null;
        if (!registry.containsKey(path)) {
            listenersOfPath = new CopyOnWriteArrayList<>();
            registry.put(path, listenersOfPath);
        } else {
            listenersOfPath = registry.get(path);
        }

        listenersOfPath.add(onChanged);
    }

    public synchronized void registerWithMultiPaths(String[] paths, IExchangerListener onChanged) {
        for (String path : paths) {
            this.registerWithPath(path, onChanged);
        }
    }

    public synchronized void registerWithTable(String table, IExchangerListener onChanged) {
        if (!tablePathMap.containsKey(table)) {
            logger.error("[registerWithTable] illegal table name : " + table);
            throw new RuntimeException("illegal table name : " + table);
        }

        String path = tablePathMap.get(table);
        this.registerWithPath(path, onChanged);
    }

    public synchronized void removeRegister(IExchangerListener subscriber) {
        for (Map.Entry<String, List<IExchangerListener>> listenersOfPath : registry.entrySet()) {
            for (IExchangerListener listener : listenersOfPath.getValue()) {
                if (listener == subscriber)
                    listenersOfPath.getValue().remove(listener);
            }
        }
    }

    public synchronized void destroy() {
        if (this.zookeeper != null && this.zookeeper.isAlive())
            this.zookeeper.close();
    }

    public Map<String, IDataFetcher> getTableDataFetcherMap() {
        return tableDataFetcherMap;
    }

    public void setTableDataFetcherMap(Map<String, IDataFetcher> tableDataFetcherMap) {
        this.tableDataFetcherMap = tableDataFetcherMap;
    }

    private synchronized void beforeUploadThroughFetcher() {
        if (tableDataFetcherMap == null || tableDataFetcherMap.size() == 0) {
            throw new IllegalStateException(" must set property : tableDataFetcherMap");
        }

        if (!this.dataFetcherInited)
            this.lazyInit();
    }

    private void scan() {
        Set<Class<IDataExchanger>> classes = traverse("com.freedom.messagebus.business.exchanger.impl");

        exchangers = new CopyOnWriteArrayList<>();

        pathExchangeMap = new ConcurrentHashMap<>(classes.size());
        tablePathMap = new ConcurrentHashMap<>(classes.size());
        paths = new CopyOnWriteArrayList<>();

        try {
            for (Class<IDataExchanger> clazz : classes) {
                Annotation[] annotations = clazz.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().getName().equals(Exchanger.class.getName())) {
                        Exchanger metaData = (Exchanger) annotation;
                        if (metaData != null) {
                            IDataExchanger exchanger = null;
                            Constructor ctor = clazz.getConstructor(LongLiveZookeeper.class,
                                                                    String.class);
                            exchanger = (IDataExchanger) ctor.newInstance(zookeeper, metaData.path());

                            paths.add(metaData.path());
                            exchangers.add(exchanger);

                            if (!pathExchangeMap.containsKey(metaData.path())) {
                                pathExchangeMap.put(metaData.path(), exchanger);
                            }

                            if (!tablePathMap.containsKey(metaData.path())) {
                                tablePathMap.put(metaData.table(), metaData.path());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("[scan] occurs a Exception : " + e.getMessage());
        }

        if (logger.isDebugEnabled()) {
            logger.debug(" ******exchangers size : " + exchangers.size());
            logger.debug(" ******pathExchangeMap size : " + pathExchangeMap.size());
            logger.debug(" ******tableExchangeMap size : " + tablePathMap.size());
        }
    }

    private void watchZookeeper() {
        this.zookeeper.watchPaths(this.paths.toArray(new String[this.paths.size()]), new IConfigChangedListener() {
            @Override
            public void onChanged(String path, byte[] newData, ZKEventType eventType) {
                Object deserializedObj = null;
                if (path == null || !pathExchangeMap.containsKey(path)) {
                    logger.error("[onChanged] path is null or pathExchangeMap don't contain path : " + path);
                    return;
                }

                IDataExchanger exchanger = pathExchangeMap.get(path);

                try {
                    deserializedObj = exchanger.download(newData);
                } catch (IOException e) {
                    logger.error("[watchZookeeper] occurs a IOException : " + e.getMessage());
                }

                if (registry.containsKey(path)) {
                    List<IExchangerListener> listeners = registry.get(path);
                    for (IExchangerListener listener : listeners) {
                        listener.onZKPathChanged(path, deserializedObj);
                    }
                }
            }
        });
    }

    private static Set<Class<IDataExchanger>> traverse(String packageStr) {
        Set<Class<IDataExchanger>> classes = new LinkedHashSet<>();
        boolean recursive = true;
        String packageDirName = packageStr.replace('.', '/');

        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    //get physical path
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddClassesInPackageByFile(packageStr, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    String packageName = packageStr;
                    jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();

                        if (name.charAt(0) == '/') {
                            name = name.substring(1);
                        }

                        if (name.startsWith(packageDirName)) {
                            int idx = name.lastIndexOf('/');

                            if (idx != -1) {
                                packageName = name.substring(0, idx).replace('/', '.');
                            }

                            if ((idx != -1) || recursive) {
                                if (name.endsWith(".class") && !entry.isDirectory()) {
                                    String className = name.substring(packageName.length() + 1, name.length() - 6);

                                    try {
                                        classes.add((Class<IDataExchanger>) Class.forName(packageName + "." + className));
                                    } catch (ClassNotFoundException e) {
                                        logger.error("[traverse] occurs a ClassNotFoundException : " + e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            logger.error("[scan] occurs a IOException : " + e.getMessage());
            throw new RuntimeException(e);
        }

        return classes;
    }

    private static void findAndAddClassesInPackageByFile(String packageName,
                                                         String packagePath,
                                                         final boolean recursive,
                                                         Set<Class<IDataExchanger>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("the package :" + packageName + "has no files");
            return;
        }

        File[] dirFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (recursive && pathname.isDirectory()) ||
                    (pathname.getName().endsWith(".class"));
            }
        });

        for (File file : dirFiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
                                                 file.getAbsolutePath(),
                                                 recursive,
                                                 classes);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);

                try {
                    Class clazz = Thread.currentThread().
                        getContextClassLoader().loadClass(packageName + "." + className);

                    Annotation[] temp = clazz.getDeclaredAnnotations();

                    if (clazz.getModifiers() == Modifier.ABSTRACT)
                        continue;

                    //fetch the class that implemented the interface `IService`
                    Class[] ifClasses = clazz.getSuperclass().getInterfaces();
                    if (ifClasses == null || ifClasses.length == 0)
                        continue;

                    for (Class ifclass : ifClasses) {
                        if (ifclass.getName().equals(IDataExchanger.class.getName()))
                            classes.add(clazz);
                    }

                } catch (ClassNotFoundException e) {
                    logger.error("[findAndAddClassesInPackageByFile] occurs a ClassNotFoundException : "
                                     + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

        }
    }

    private void lazyInit() {
        //init every exchanger's dataFetcher property
        try {
            for (Map.Entry<String, IDataFetcher> item : tableDataFetcherMap.entrySet()) {
                String tbName = item.getKey();
                if (!tablePathMap.containsKey(tbName))
                    continue;

                String path = tablePathMap.get(tbName);
                IDataExchanger exchanger = pathExchangeMap.get(path);

                Class<? extends IDataExchanger> clazz = exchanger.getClass();
                Field fetcherField = clazz.getField("dataFetcher");
                fetcherField.setAccessible(true);
                fetcherField.set(exchanger, item.getValue());
            }

            this.dataFetcherInited = true;
        } catch (NoSuchFieldException e) {
            logger.error("[lazyInit] occurs a NoSuchFieldException : " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("[lazyInit] occurs a IllegalAccessException : " + e.getMessage());
        }
    }
}
