package com.freedom.messagebus.business.exchanger;

import com.freedom.messagebus.common.Constants;
import com.freedom.messagebus.interactor.pubsub.IDataConverter;
import com.freedom.messagebus.interactor.pubsub.IPubSubListener;
import com.freedom.messagebus.interactor.pubsub.IPubSuber;
import com.freedom.messagebus.interactor.pubsub.PubSuberFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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

    private List<String>                      channels;
    private List<IDataExchanger>              exchangers;
    private Map<String, IDataExchanger>       channelExchangeMap;
    private Map<String, String>               tableChannelMap;
    private Map<String, IDataFetcher>         tableDataFetcherMap;
    private Map<String, ChannelListenerEntry> registry;
    private IPubSuber                         pubsuber;
    private boolean dataFetcherInited = false;

    public ExchangerManager(String pubsuberHost, int pubsuberPort) {
        this.pubsuber = PubSuberFactory.createPubSuber();
        this.pubsuber.setHost(pubsuberHost);
        this.pubsuber.setPort(pubsuberPort);
        this.pubsuber.open();

        boolean isAlive = this.pubsuber.isAlive();
        if (!isAlive)
            return;

        scan();

        registry = new ConcurrentHashMap<>();

        //add external path
        this.channels.add(Constants.PUBSUB_AUTH_CHANNEL);

        watchPubSuber();
    }

    public boolean isZKAlive() {
        return this.pubsuber.isAlive();
    }

    public void retryConnect(String pubsuberHost, int pubsuberPort) {
        if (this.pubsuber != null && this.pubsuber.isAlive())
            this.destroy();

        this.pubsuber = null;
        this.pubsuber = PubSuberFactory.createPubSuber();
        this.pubsuber.setHost(pubsuberHost);
        this.pubsuber.setPort(pubsuberPort);
        this.pubsuber.open();
    }

    public void uploadAll() {
        this.beforeUploadThroughFetcher();

        try {
            for (IDataExchanger exchanger : this.exchangers) {
                exchanger.upload();
            }
        } catch (IOException e) {
            logger.error("[uploadAll] occurs a IOException : " + e.getMessage());
        }
    }

    public void uploadWithChannel(String channel) throws IOException {
        this.beforeUploadThroughFetcher();

        if (!this.channelExchangeMap.containsKey(channel)) {
            throw new RuntimeException("[uploadWithChannel] the path : " + channel);
        }

        IDataExchanger exchanger = this.channelExchangeMap.get(channel);
        exchanger.upload();
    }

    public void uploadWithTable(String tbName) throws IOException {
        this.beforeUploadThroughFetcher();

        if (!this.tableChannelMap.containsKey(tbName))
            throw new RuntimeException("[uploadWithTable] the table name : " + tbName);

        String path = this.tableChannelMap.get(tbName);
        this.uploadWithChannel(path);
    }

    public void uploadWithChannel(String channel, byte[] data) throws IOException {
        if (!channelExchangeMap.containsKey(channel)) {
            logger.error("[uploadWithChannel] illegal path : " + channel);
            throw new RuntimeException("illegal path : " + channel);
        }

        IDataExchanger exchanger = channelExchangeMap.get(channel);
        exchanger.upload(data);
    }

    public void uploadWithTable(String tbName, byte[] data) throws IOException {
        if (!tableChannelMap.containsKey(tbName)) {
            logger.error("[uploadWithTable] illegal table name : " + tbName);
            throw new RuntimeException("illegal table name : " + tbName);
        }

        String path = tableChannelMap.get(tbName);

        channelExchangeMap.get(path).upload(data);
    }

    public Object downloadWithChannel(String channel) throws IOException {
        if (!channelExchangeMap.containsKey(channel)) {
            logger.error("[downloadWithChannel] illegal channel : " + channel);
            throw new RuntimeException("illegal channel : " + channel);
        }

        return channelExchangeMap.get(channel).download();
    }

    public Object downloadWithTable(String tbName) throws IOException {
        if (!tableChannelMap.containsKey(tbName)) {
            logger.error("[downloadWithTable] illegal table name : " + tbName);
            throw new RuntimeException("illegal table name : " + tbName);
        }

        String path = tableChannelMap.get(tbName);

        return this.downloadWithChannel(path);
    }

    public void registerWithChannel(String appId, IExchangerListener onChanged, String channel) {
        if (!registry.containsKey(appId)) {
            ChannelListenerEntry entry = new ChannelListenerEntry();
            entry.setOnChanged(onChanged);
            List<String> channels = new ArrayList<>();
            channels.add(channel);
            entry.setChannels(channels);

            registry.put(appId, entry);
        } else {
            ChannelListenerEntry entry = registry.get(appId);
            entry.getChannels().add(channel);
        }
    }

    public void registerWithMultiChannels(String appId, IExchangerListener onChanged, String[] channels) {
        for (String channel : channels) {
            this.registerWithChannel(appId, onChanged, channel);
        }
    }

    public void removeRegister(String appId) {
        this.registry.remove(appId);
        if (this.registry.size() == 0) {
            this.pubsuber.close();
        }
    }

    public void destroy() {
        if (this.pubsuber != null && this.pubsuber.isAlive())
            this.pubsuber.close();
    }

    public Map<String, IDataFetcher> getTableDataFetcherMap() {
        return tableDataFetcherMap;
    }

    public void setTableDataFetcherMap(Map<String, IDataFetcher> tableDataFetcherMap) {
        this.tableDataFetcherMap = tableDataFetcherMap;
    }

    private void beforeUploadThroughFetcher() {
        if (tableDataFetcherMap == null || tableDataFetcherMap.size() == 0) {
            throw new IllegalStateException(" must set property : tableDataFetcherMap");
        }

        if (!this.dataFetcherInited)
            this.lazyInit();
    }

    private void scan() {
        Set<Class<IDataExchanger>> classes = traverse("com.freedom.messagebus.business.exchanger.impl");

        exchangers = new CopyOnWriteArrayList<>();

        channelExchangeMap = new ConcurrentHashMap<>(classes.size());
        tableChannelMap = new ConcurrentHashMap<>(classes.size());
        channels = new CopyOnWriteArrayList<>();
        IDataConverter converter = PubSuberFactory.createConverter();

        try {
            for (Class<IDataExchanger> clazz : classes) {
                Annotation[] annotations = clazz.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().getName().equals(Exchanger.class.getName())) {
                        Exchanger metaData = (Exchanger) annotation;
                        if (metaData != null) {
                            IDataExchanger exchanger = null;
                            Constructor ctor = clazz.getConstructor(IPubSuber.class,
                                                                    String.class);
                            exchanger = (IDataExchanger) ctor.newInstance(pubsuber, metaData.path());
                            Class<? extends IDataExchanger> clazzOfExchanger = exchanger.getClass();

                            //set dataConverter
                            Field converterField = clazzOfExchanger.getField("dataConverter");
                            converterField.setAccessible(true);
                            converterField.set(exchanger, converter);

                            channels.add(metaData.path());
                            exchangers.add(exchanger);

                            if (!channelExchangeMap.containsKey(metaData.path())) {
                                channelExchangeMap.put(metaData.path(), exchanger);
                            }

                            if (!tableChannelMap.containsKey(metaData.path())) {
                                tableChannelMap.put(metaData.table(), metaData.path());
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
            logger.debug(" ******channelExchangeMap size : " + channelExchangeMap.size());
            logger.debug(" ******tableExchangeMap size : " + tableChannelMap.size());
        }
    }

    private void watchPubSuber() {
        this.pubsuber.watch(this.channels.toArray(new String[this.channels.size()]), new IPubSubListener() {

            @Override
            public void onChange(String channel, byte[] data, Map<String, Object> params) {
                Object deserializedObj = null;
                if (channel == null || !channelExchangeMap.containsKey(channel)) {
                    logger.error("[onChanged] channel is null or channelExchangeMap don't contain path : " + channel);
                    return;
                }

                IDataExchanger exchanger = channelExchangeMap.get(channel);

                try {
                    deserializedObj = exchanger.download(data);
                } catch (IOException e) {
                    logger.error("[watchPubSuber] occurs a IOException : " + e.getMessage());
                }

                for (ChannelListenerEntry entry : registry.values()) {
                    if (entry.getChannels().contains(channel)) {
                        entry.getOnChanged().onChannelDataChanged(channel, deserializedObj);
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
                if (!tableChannelMap.containsKey(tbName))
                    continue;

                String path = tableChannelMap.get(tbName);
                IDataExchanger exchanger = channelExchangeMap.get(path);

                Class<? extends IDataExchanger> clazz = exchanger.getClass();

                //set dataFetcher
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

    private static final class ChannelListenerEntry {
        private List<String>       channels;
        private IExchangerListener onChanged;

        public ChannelListenerEntry() {
        }

        public List<String> getChannels() {
            return channels;
        }

        public void setChannels(List<String> channels) {
            this.channels = channels;
        }

        public IExchangerListener getOnChanged() {
            return onChanged;
        }

        public void setOnChanged(IExchangerListener onChanged) {
            this.onChanged = onChanged;
        }

    }

}
