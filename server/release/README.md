#overview
本文档用于说明 `messagebus-server`在 **FreeBSD** 环境下的安装、部署、启动步骤并展示如何将其构建成 `unix-like-service` 的方式运行

##依赖组件
* rabbitmq(3.3.4+)
* jetty(9.2.3+)
* Java_SDK(1.7+)
* Zookeeper
* mysql

> 在启动message bus server之前请确认这些组件已被正确安装并且运行良好。


##准备工作

* 在操作系统任意一个文件夹内新建 messagebus-server 文件夹(推荐/usr/local)

> 此处以/usr/local/messagebus-server 目录作为示例


```
mkdir /usr/local/messagebus-server
```

* 将本文档所在目录中的所有内容全部拷贝至 `/usr/local/messagebus-server`，并进入该目录：

```
cd /usr/local/messagebus-server
```

* 修改配置文件 `message.server.config.properties`

```
vi /usr/local/messagebus-server/conf/message.server.config.properties
```
* 将配置文件复制一份到 `/etc`文件夹下

```
cp /usr/local/messagebus-server/conf/message.server.config.properties /etc/message.server.config.properties
```

* (optional) 如果路径不为 `/usr/local/messagebus-server`，打开启动脚本，修改 `MESSAGEBUS_SERVER_HOME` 环境变量为实际路径

```
vi ${实际路径}/messagebus-server.sh
```


* 复制启动脚本到 `/etc/init.d`目录下

```
cp /usr/local/messagebus-server/messagebus-server.sh /etc/init.d/messagebus-server
```

* 赋予其所有用户的执行权限

```
chmod +x /etc/init.d/messagebus-server
```

* 核对相关配置项（如果不一致，请修改 `/etc/init.d/messagebus-server` 文件）

```
service messagebus-server check
```

可以看到如下图的打印输出：

![img 1][1]

对比确认无误。到此，**准备工作已经完成**


##run

* 运行message bus server (内部采用 `start-stop-daemon` 启动)

```
service messagebus-server start
```

启动没有日志输出，只会输出结果：**started** 启动日志，见后面的配置文件。

* 检查运行结果（启动成功，会往 `/var/run/` 写入一个 `messagebus-server.pid` 文件作为进程号标示）

```
service messagebus-server status
```
将会看到如下带有pid的输出：

![img 2][2]

查看该进程状态信息

```
ps u 12019
```

![img 3][3]

> 命令 `check` 与 `status` 执行的任务相同

##stop
* 关闭message bus server（与此同时 mq server也一同被关闭，而其他组件状态保持不变）

```
service messagebus-server stop
```

通过 `status` 命令查看pid已被kill掉了

##restart
* 重启命令：

```
service messagebus-server restart
```

##日志记录配置
日志配置文件存放于：`/usr/local/messagebus-server/conf/log4j.properties` 

默认按天记录messagebus-server的所有日志，如无需要可不必更改。日志框架基于 `log4j` 提供两种日志记录类型：

* 按文件大小拆分 
* 按日期划分 (默认)

记录的日志内容包含：

- 启动日志
- 运行时日志
- 关闭日志

日志路径默认存放于： `/usr/local/messagebus-server/log/server/server.log`

修改日志配置项：

```
vi /usr/local/messagebus-server/conf/log4j.properties
```





[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/server/releaseimages/release-service-check.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/server/releaseimages/release-service-status.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/server/releaseimages/release-ps.png