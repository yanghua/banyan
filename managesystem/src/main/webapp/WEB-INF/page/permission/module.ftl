<div id="moduleTableContainer"></div>
<#include "/WEB-INF/widget/resource/jtable.ftl" />
<script>
    var MODULE = {
        parentModule : null
    };

    $(document).ready(function () {
        $('#moduleTableContainer').jtable({
            title: '模块列表',
            paging: true,
            pageSize: 10,
            actions: {
                listAction: '/permission/module/list',
                createAction: '/permission/module/create',
                updateAction: '/permission/module/update',
                deleteAction: '/permission/module/delete'
            },
            recordAdded: function (event, data) {
                getParentModules(null, true);
            },
            recordsDeleted: function (event, data) {
                getParentModules(null, true);
            },
            recordUpdated: function (event, data) {
                getParentModules(null, true);
            },
            fields: {
                moduleCode: {
                    title: "资源编码",
                    key: true,
                    list: false,
                    create: false,
                    edit: false
                },
                moduleName: {
                    title: '资源名称',
                    width: '20%',
                    edit: true
                },
                moduleValue: {
                    title: '资源标识',
                    width: '20%',
                    edit: true
                },
                linkUrl: {
                    title: '链接地址',
                    width: '20%',
                    edit: true
                },
                parentModule: {
                    title: '上级模块',
                    width : '20%',
                    options: getParentModules
                },
                sortIndex : {
                    title: '显示顺序',
                    width: '20%'
                }
            }
        });

        $('#moduleTableContainer').jtable('load');
    });

    var getParentModules = function (data, isUpdate) {
        isUpdate = isUpdate || false;
        if (MODULE.parentModule != null && !isUpdate)
            return MODULE.parentModule;
        else {
            $.ajax({
                url: '/permission/module/parentModuleInfo',
                type: "POST",
                contentType: 'application/json;charset=utf-8',
                async: false,
                cache: false,
                success: function (data) {
                    console.log(data);
                    if (data && data.Options) {
                        data.Options.push({
                            Value           : -1,
                            DisplayText     : '无'
                        });
                        MODULE.parentModule = data.Options;
                    } else {
                        MODULE.parentModule = [];
                    }
                },
                error: function (err) {
                    MODULE.parentModule = JSON.parse(err);
                }
            });

            return MODULE.parentModule;
        }
    }
</script>
