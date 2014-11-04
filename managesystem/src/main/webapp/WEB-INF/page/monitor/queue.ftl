<div id="queueTableContainer"></div>
<#include "/WEB-INF/widget/resource/jtable.ftl" />
<script>
    $(document).ready(function () {
        $('#queueTableContainer').jtable({
            title: '队列列表',
            actions: {
                listAction: '/monitor/queue/list'
            },
            fields: {
                name: {
                    title: '队列名称',
                },
                state: {
                    title: '状态'
                },
                durable: {
                    title: '是否持久化'
                },
                autoDelete: {
                    title: '是否自动删除'
                },
                ramMsgCount: {
                    title: '滞留消息'
                },
                avgEgressRate: {
                    title: '平均出队速率'
                },
                avgIngressRate: {
                    title: '平均入队速率'
                },
                memSizeOfMB: {
                    title: '内存占用(MB)'
                }
            }
        });

        $('#queueTableContainer').jtable('load');
    });
</script>