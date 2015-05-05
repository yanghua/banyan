<div id="queueMsgChart" class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.queuedMsgChart}</li>
            <li class="refreshBtn"><a href="<@ofbizUrl>#</@ofbizUrl>">${uiLabelMap.CommonRefresh}</a></li>
        </ul>
    </div>
    <div class="" id="div_queued_msg_chart" style="height: 250px"></div>
</div>
<script type="text/javascript">
    var queuedMsgChartOption = {
        title: {
            text: '',
            subtext: ''
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: ['Ready', 'Unacked', 'total']
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
                name: 'Ready',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            },
            {
                name: 'Unacked',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            },
            {
                name: 'total',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            }

        ]
    };

    function renderQueuedMsgChart() {
        //x
        if (timeTagArr) {
            queuedMsgChartOption.xAxis[0].data = timeTagArr;
        }

        //y
        if (commonMQServerOverviewObj) {
            queuedMsgChartOption.series[0].data.shift();
            queuedMsgChartOption.series[0].data.push(commonMQServerOverviewObj.queue_totals.messages_ready);
            queuedMsgChartOption.series[1].data.shift();
            queuedMsgChartOption.series[1].data.push(commonMQServerOverviewObj.queue_totals.messages_unacknowledged);
            queuedMsgChartOption.series[2].data.shift();
            queuedMsgChartOption.series[2].data.push(commonMQServerOverviewObj.queue_totals.messages);

            queuedMsgChartObj.setOption(queuedMsgChartOption);
        }
    }

</script>

