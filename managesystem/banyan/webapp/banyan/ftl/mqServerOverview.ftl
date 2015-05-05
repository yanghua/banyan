<div id="div_mqServerOverview" class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.mqServerOverview}</li>
            <li class="refreshBtn"><a href="<@ofbizUrl>#</@ofbizUrl>">${uiLabelMap.CommonRefresh}</a></li>
        </ul>
    </div>
    <div style="height: 250px;">
        <table class="basic-table">
            <tbody>
            <tr>
                <td class="label"><span>MQ服务器版本:</span></td>
                <td id="mqServerVersion"></td>
            </tr>
            <tr>
                <td class="label"><span>Connection 数目:</span></td>
                <td id="connectionNum"></td>
            </tr>
            <tr>
                <td class="label"><span>Channel 数目:</span></td>
                <td id="channelNum"></td>
            </tr>
            <tr>
                <td class="label"><span>Exchange 数目:</span></td>
                <td id="exchangeNum"></td>
            </tr>
            <tr>
                <td class="label"><span>Queue 数目:</span></td>
                <td id="queueNum"></td>
            </tr>
            <tr>
                <td class="label"><span>Consumer 数目:</span></td>
                <td id="consumerNum"></td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
<script language="JavaScript" type="text/javascript">
    var commonMQServerOverviewObj = null;
    var queuedMsgChartObj = null;
    var queuedMsgRateChartObj = null;
    var msgRateChartObj = null;

    var interval = 10;
    var timestampArr = null;
    var timeTagArr = null;

    function initTimestampArr() {
        var commonTimestamp = Date.now();
        timestampArr = [];
        for (var i = 7; i > 0; i--) {
            timestampArr.push(commonTimestamp - (i * interval * 1000));
        }
    }

    function buildTimeTagArr() {
        timeTagArr = [];
        for (var i = 0; i < timestampArr.length; i++) {
            var date = new Date(timestampArr[i]);
            var tag = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
            timeTagArr.push(tag);
        }
    }

    function requestMQServerOverview() {
        jQuery.ajax({
            url: 'getMQServerOverview',
            type: 'GET',
            async: false,
            success: function (resp) {
                if (resp) {
                    commonMQServerOverviewObj = resp;
                }
            }
        });
    }

    function renderMQServerOverview() {
        if (commonMQServerOverviewObj) {
            jQuery('#mqServerVersion').text(commonMQServerOverviewObj.rabbitmq_version);
            jQuery('#connectionNum').text(commonMQServerOverviewObj.object_totals.connections);
            jQuery('#channelNum').text(commonMQServerOverviewObj.object_totals.channels);
            jQuery('#exchangeNum').text(commonMQServerOverviewObj.object_totals.exchanges);
            jQuery('#queueNum').text(commonMQServerOverviewObj.object_totals.queues);
            jQuery('#consumerNum').text(commonMQServerOverviewObj.object_totals.consumers);
        }
    }

    function refreshAll() {
        requestMQServerOverview();
        initTimestampArr();
        buildTimeTagArr();
        renderMQServerOverview();
        rendMsgRateChart();
        renderQueuedMsgChart();
        rendQueuedMsgRateChart();
    }

    jQuery(document).ready(function () {
        jQuery('.refreshBtn').click(function () {
            refreshAll();

            return false;
        });

        require.config({
            paths: {
                echarts: '<@ofbizContentUrl>/banyan/assets/echarts-2.2.1/build/dist</@ofbizContentUrl>'
            }
        });

        require([
                    'echarts',
                    'echarts/chart/line'
                ],
                function (ec) {
                    msgRateChartObj = ec.init(document.getElementById('div_msg_rate_chart'));
                    queuedMsgChartObj = ec.init(document.getElementById('div_queued_msg_chart'));
                    queuedMsgRateChartObj = ec.init(document.getElementById('div_queued_msg_rate_chart'));

                    refreshAll();

                    setInterval(refreshAll, 10 * 1000);
                });
    });

</script>

