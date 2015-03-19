package com.messagebus.server.daemon;

import com.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ServiceLoader {

    private static final Log    logger     = LogFactory.getLog(ServiceLoader.class);
    private static final String packageStr = "com.messagebus.server.daemon.impl";

    private static volatile ServiceLoader instance;

    private Map<String, IService> runOnceServiceMap;
    private Map<String, IService> scheduleCycleServiceMap;
    private Map<String, Object>   context;

    private ServiceLoader(Map<String, Object> context) {
        this.context = context;
        this.scan();
    }

    public static ServiceLoader getInstance(Map<String, Object> context) {
        if (instance == null) {
            synchronized (ServiceLoader.class) {
                if (instance == null) {
                    instance = new ServiceLoader(context);
                }
            }
        }

        return instance;
    }

    public void launch() {
        if (runOnceServiceMap.size() != 0) {
            ExecutorService executorService =
                new ExceptionCatchThreadPool(runOnceServiceMap.size());
            for (Map.Entry<String, IService> entry : this.runOnceServiceMap.entrySet()) {
                executorService.submit((Runnable) entry.getValue());
            }
        }

        if (scheduleCycleServiceMap.size() != 0) {
            ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(scheduleCycleServiceMap.size());
            for (Map.Entry<String, IService> entry : this.scheduleCycleServiceMap.entrySet()) {
                scheduledExecutorService.scheduleAtFixedRate((Runnable) entry.getValue(), 0, 10, TimeUnit.SECONDS);
            }
        }
    }

    private void scan() {
        Set<Class<IService>> classes = this.traverse();

        runOnceServiceMap = new ConcurrentHashMap<>();
        scheduleCycleServiceMap = new ConcurrentHashMap<>();

        try {
            for (Class<IService> clazz : classes) {
                Annotation[] annotations = clazz.getAnnotations();

                boolean hasParamedCstor = this.judgeSpecialCstor(clazz);

                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().getName().equals(DaemonService.class.getName())) {
                        DaemonService daemonService = (DaemonService) annotation;
                        if (daemonService != null) {
                            IService service = null;

                            switch (daemonService.policy()) {
                                case ONCE: {
                                    if (hasParamedCstor) {
                                        Constructor<IService> constructor = clazz.getConstructor(Map.class);
                                        service = constructor.newInstance(this.context);
                                    } else
                                        service = clazz.newInstance();

                                    runOnceServiceMap.put(daemonService.value(), service);
                                }
                                break;

                                case CYCLE_SCHEDULED: {
                                    if (hasParamedCstor) {
                                        Constructor<IService> constructor = clazz.getConstructor(Map.class);
                                        service = constructor.newInstance(this.context);
                                    } else
                                        service = clazz.newInstance();

                                    scheduleCycleServiceMap.put(daemonService.value(), service);
                                }
                                break;

                                default:
                                    throw new UnsupportedOperationException("unsupported daemon service policy : "
                                                                                + daemonService.policy());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "[scan]");
        }
    }

    private Set<Class<IService>> traverse() {
        Set<Class<IService>> classes = new LinkedHashSet<>();
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
                                        classes.add((Class<IService>) Class.forName(packageName + "." + className));
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

    private void findAndAddClassesInPackageByFile(String packageName,
                                                  String packagePath,
                                                  final boolean recursive,
                                                  Set<Class<IService>> classes) {
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

                    //fetch the class that implemented the interface `IService`
                    Class[] ifClasses = clazz.getSuperclass().getInterfaces();
                    if (ifClasses == null || ifClasses.length == 0)
                        continue;

                    for (Class ifclass : ifClasses) {
                        if (ifclass.getName().equals(IService.class.getName()))
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

    private boolean judgeSpecialCstor(Class<IService> clazz) {
        Constructor[] cstors = clazz.getConstructors();
        for (Constructor cstor : cstors) {
            Class[] paramClasses = cstor.getParameterTypes();
            if (paramClasses.length == 1 && paramClasses[0].getName().equals(Map.class.getName()))
                return true;
        }

        return false;
    }
}
