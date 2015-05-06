#overview
消息总线管控台，用于提供对RabbitMQ的内部信息可视化、消息总线核心实体的管理、消息总线管控指令的下发等功能。
与此同时它也是一些监控服务的运行容器以及消息总线对外提供服务API的web容器。

控制台展示：
![img 1][1]

队列管理展示：
![img 2][2]


##install&deploy
消息总线管控台基于[Apache ofbiz](https://ofbiz.apache.org/)构建。因此，下载并运行Apache ofbiz是必备前提！以下步骤，都基于此前提，不再赘述。

* 将该README文件同目录下的文件夹 `banyan` 整个拷贝到Apache ofbiz工程根目录下的`hot-deploy`文件夹下。

为了区分消息总线管控台的表集合，消息总线管控台拥有独立的数据库`banyan_DB`。所以你还必须配置数据库信息

* 在${ofbiz_basePath}/framework/entity/config/entityengine.xml配置文件内，增加消息总线管控台的数据库配置：

如果基于mysql，则增加如下配置：

```xml
<!-- specify a special database for banyan -->
    <datasource name="localmysqlBanyan"
                helper-class="org.ofbiz.entity.datasource.GenericHelperDAO"
                field-type-name="mysql"
                check-on-start="true"
                add-missing-on-start="true"
                check-pks-on-start="false"
                use-foreign-keys="true"
                join-style="ansi-no-parenthesis"
                alias-view-columns="false"
                drop-fk-use-foreign-key-keyword="true"
                table-type="InnoDB"
                character-set="utf8"
                collate="utf8_general_ci">
        <read-data reader-name="tenant"/>
        <read-data reader-name="seed"/>
        <read-data reader-name="seed-initial"/>
        <read-data reader-name="demo"/>
        <read-data reader-name="ext"/>
        <read-data reader-name="ext-test"/>
        <read-data reader-name="ext-demo"/>
        <inline-jdbc
                jdbc-driver="com.mysql.jdbc.Driver"
                jdbc-uri="jdbc:mysql://172.16.206.17/banyan_DB?autoReconnect=true"
                jdbc-username="root"
                jdbc-password="123456"
                isolation-level="ReadCommitted"
                pool-minsize="2"
                pool-maxsize="250"
                time-between-eviction-runs-millis="600000"/><!-- Please note that at least one person has experienced a problem with this value with MySQL
                and had to set it to -1 in order to avoid this issue.
                For more look at http://markmail.org/thread/5sivpykv7xkl66px and http://commons.apache.org/dbcp/configuration.html-->
        <!-- <jndi-jdbc jndi-server-name="localjndi" jndi-name="java:/MySqlDataSource" isolation-level="Serializable"/> -->
    </datasource>
``` 	
 
并在相应的delegator中增加如下配置：

```xml
<group-map group-name="org.ofbiz.banyan" datasource-name="localmysqlBanyan" />
```

如果基于内嵌的数据库derby，则增加如下配置：

```xml
<datasource name="localderbyBanyan"
                helper-class="org.ofbiz.entity.datasource.GenericHelperDAO"
                schema-name="OFBIZ"
                field-type-name="derby"
                check-on-start="true"
                add-missing-on-start="true"
                use-pk-constraint-names="false"
                use-indices-unique="false"
                alias-view-columns="false"
                use-order-by-nulls="true"
                offset-style="fetch">
        <read-data reader-name="tenant"/>
        <read-data reader-name="seed"/>
        <read-data reader-name="seed-initial"/>
        <read-data reader-name="demo"/>
        <read-data reader-name="ext"/>
        <read-data reader-name="ext-test"/>
        <read-data reader-name="ext-demo"/>
        <!-- beware use-indices-unique="false" is needed because of Derby bug with null values in a unique index -->
        <inline-jdbc
                jdbc-driver="org.apache.derby.jdbc.EmbeddedDriver"
                jdbc-uri="jdbc:derby:banyan_DB;create=true"
                jdbc-username="ofbiz"
                jdbc-password-lookup="derby-ofbiz"
                isolation-level="ReadCommitted"
                pool-minsize="2"
                pool-maxsize="250"
                time-between-eviction-runs-millis="600000"/>
        <!-- <jndi-jdbc jndi-server-name="localjndi" jndi-name="java:/DerbyDataSource" isolation-level="ReadCommitted"/> -->
    </datasource>
```

并在相应的delegator中增加如下配置：

```xml
<group-map group-name="org.ofbiz.banyan" datasource-name="localderbyBanyan" />
```

数据库配置完成，为了能启动控制台，需要配置消息总线依赖的两个组件的 `RabbitMQ`,`redis`/`zookeeper`的host以及port

* 请定位到 ${ofbiz_basePath}/hot-deploy/banyan/config/MessagebusConfig.properties，配置如下几个参数参数：`messagebus.mq.host`,`messagebus.pubsuberHost`,`messagebus.pubsuberPort`

下面进行基础数据以及测试数据的初始化：

* 请定位到${ofbiz_basePath}，分别执行`ant load-seed` 以及 `ant load-demo`

接下来就可以启动ofbiz了：

* 请定位到${ofbiz_basePath}，执行`ant start` 

登录消息总线的管控台：

* 打开浏览器：访问URL：http://localhost:8080/banyan/control/main

##others

问题追踪，请查看ofbiz的日志，位于${ofbiz_basePath}/runtime/logs

如果你不想基于Apache ofbiz构建web管控台，你可以选择自己构建，在本文件同目录下有完整的mysql数据库sql文件`banyan_DB.sql`。

有任何疑问请联系我：

* 新浪微博:@vino_Yang
* 邮箱:yanghua1127(at)gmail.com
* 消息总线专栏:[消息总线专栏](http://blog.csdn.net/yanghua_kobe/article/category/2898357)

[1]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/overview/webconsole-dashboard.png
[2]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/overview/webconsole-queueManage.png
