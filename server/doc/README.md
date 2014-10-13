#overview
本文档用于说明 `messagebus-server`在 FreeBSD 环境下的安装、部署、启动步骤，此处展示如何将其以类 `unix-service` 的方式运行

##准备工作
* 编译server jar:(此打包方式包含依赖module)

```
cd ${messagebus-server's path}/
mvn assembly:assembly
```
* 在操作系统任意一个文件夹内新建 messagebus-server 文件夹(推荐/usr/local)

> 此处以/usr/local/messagebus-server 目录作为示例


```
mkdir /usr/local/messagebus-server
```

##run

##stop

