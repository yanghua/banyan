<div id="exchangeTableContainer"></div>
<#include "/WEB-INF/widget/resource/jtable.ftl" />
<script>
    $(document).ready(function () {
        $('#exchangeTableContainer').jtable({
            title: '交换器列表',
            actions: {
                listAction: '/monitor/exchange/list'
            },
            fields: {
                name: {
                    title: '名称',
                    width: '30%'
                },
                type: {
                    title: '类型',
                    width: '10%'
                },
                durable: {
                    title: '持久化',
                    width: '10%'
                },
                autoDelete: {
                    title: '自动删除',
                    width: '10%'
                },
                internal: {
                    title: '内部',
                    width: '10%'
                }
            }
        });

        $('#exchangeTableContainer').jtable('load');
    });
</script>