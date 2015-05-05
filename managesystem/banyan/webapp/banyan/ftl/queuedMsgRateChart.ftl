<div id="queuedMsgRateChart" class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.queuedMsgRateChart}</li>
            <li class="refreshBtn"><a href="<@ofbizUrl>#</@ofbizUrl>">${uiLabelMap.CommonRefresh}</a></li>
        </ul>
    </div>
    <div class="" id="div_queued_msg_rate_chart" style="height: 250px"></div>
</div>
<script type="text/javascript">
    var queuedMsgRateChartOption = {
        title: {
            text: '',
            subtext: ''
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: ['message', 'ready', 'unacknowledged']
        },
        toolbox: {
            show: true,
            feature: {
                mark: {show: false},
                dataView: {show: true, readOnly: false},
                magicType: {show: false, type: ['line', 'bar']},
                restore: {show: false},
                saveAsImage: {show: true}
            }
        },
        calculable: true,
        xAxis: [
            {
                type: 'category',
                boundaryGap: false,
                data: ['00:00', '00:00', '00:00', '00:00', '00:00', '00:00', '00:00']
            }
        ],
        yAxis: [
            {
                type: 'value',
                axisLabel: {
                    formatter: '{value} '
                }
            }
        ],
        series: [
            {
                name: 'message',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            },
            {
                name: 'ready',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            },
            {
                name: 'unacknowledged',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            }
        ]
    };

    function rendQueuedMsgRateChart() {
        //x
        if (timeTagArr) {
            queuedMsgRateChartOption.xAxis[0].data = timeTagArr;
        }

        //y
        if (commonMQServerOverviewObj) {
            if (commonMQServerOverviewObj.queue_totals.messages_details) {
                queuedMsgRateChartOption.series[0].data.shift();
                queuedMsgRateChartOption.series[0].data.push(commonMQServerOverviewObj.queue_totals.messages_details.rate);
            }

            if (commonMQServerOverviewObj.queue_totals.messages_ready_details) {
                queuedMsgRateChartOption.series[1].data.shift();
                queuedMsgRateChartOption.series[1].data.push(commonMQServerOverviewObj.queue_totals.messages_ready_details.rate);
            }

            if (commonMQServerOverviewObj.queue_totals.messages_unacknowledged_details) {
                queuedMsgRateChartOption.series[2].data.shift();
                queuedMsgRateChartOption.series[2].data.push(commonMQServerOverviewObj.queue_totals.messages_unacknowledged_details.rate);
            }

            queuedMsgRateChartObj.setOption(queuedMsgRateChartOption);
        }
    }
</script>