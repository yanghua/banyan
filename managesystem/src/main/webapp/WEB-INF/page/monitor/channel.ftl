<div id="channelTableContainer"></div>
<#include "/WEB-INF/widget/resource/jtable.ftl" />
<script>

    $(document).ready(function () {
         $('#channelTableContainer').jtable({
            title: '通道列表',
            actions: {
                listAction: '/monitor/channel/list'
            },
            fields: {
                name: {
                    title: '名称',
                    width: '40%'
                },
                userName: {
                    title: '用户名称',
                    width: '10%'
                },
                state: {
                    title: '状态',
                    width: '10%'
                },
                transactional: {
                    title: '事务模式',
                    options: { 'true' : '是', 'false' : '否'},
                    width: '3%'
                },
                confirm: {
                    title: '确认模式',
                    options: { true : '是', false : '否'},
                    width: '3%'
                },
                prefetch: {
                    title: '预取',
                    width: '4%'
                },
                unacked: {
                    title: '未应答数',
                    width: '4%'
                },
                unconfirmed: {
                    title: '未确认数',
                    width: '4%'
                }
            }
         });

        $('#channelTableContainer').jtable('load');
    });
</script>