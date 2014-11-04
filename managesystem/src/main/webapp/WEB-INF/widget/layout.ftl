<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="description" content="消息总线管理系统"/>
    <title>消息总线管理系统</title>
    <link type="text/css" rel="stylesheet" href="../../resources/stylesheet/css.css"/>
    <script type="text/javascript" src="/resources/component/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="/resources/component/layer/layer.min.js"></script>
    <script type="text/javascript" src="/resources/js/layout/layout.js"></script>
</head>
<body>
    <div class="header">
        <#include "/WEB-INF/widget/module.ftl" />
    </div>
    <div class="container">
        <#include "/WEB-INF/widget/leftbar.ftl" />
        <div class="mainbody">
            <#include "/WEB-INF/widget/visiableMenu.ftl" />
            <#include "/WEB-INF/widget/infoTip.ftl" />
            <!--
                //content page placeholder
            -->
            <#include "/WEB-INF/page/${Request.pageName}.ftl" />
        </div>
    </div>
    <div class="footer"></div>
</body>
</html>
