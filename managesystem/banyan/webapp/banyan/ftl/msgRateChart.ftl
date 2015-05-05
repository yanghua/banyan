<div id="msgRateChart" class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.msgRateChart}</li>
            <li class="refreshBtn"><a href="<@ofbizUrl>#</@ofbizUrl>">${uiLabelMap.CommonRefresh}</a></li>
        </ul>
    </div>
    <div class="" id="div_msg_rate_chart" style="height: 250px"></div>
</div>
<script type="text/javascript">
    var msgRateChartOption = {
        title: {
            text: '',
            subtext: ''
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: ['publish', 'ack', 'deliver_get', 'deliver', 'get', 'deliver_no_ack']
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
                data: ['10:10', '10:15', '10:20', '10:25', '10:30', '10:35', '10:40']
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
                name: 'publish',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            },
            {
                name: 'ack',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            },
            {
                name: 'deliver_get',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            },
            {
                name: 'deliver',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            },
            {
                name: 'get',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            },
            {
                name: 'deliver_no_ack',
                type: 'line',
                data: [0, 0, 0, 0, 0, 0, 0]
            }
        ]
    };

    function rendMsgRateChart() {
        //x
        if (timeTagArr) {
            msgRateChartOption.xAxis[0].data = timeTagArr;
        }

        //y
        if (commonMQServerOverviewObj) {
            if (commonMQServerOverviewObj.message_stats.publish_details) {
                msgRateChartOption.series[0].data.shift();
                msgRateChartOption.series[0].data.push(commonMQServerOverviewObj.message_stats.publish_details.rate);
            }

            if (commonMQServerOverviewObj.message_stats.ack_details) {
                msgRateChartOption.series[1].data.shift();
                msgRateChartOption.series[1].data.push(commonMQServerOverviewObj.message_stats.ack_details.rate);
            }

            if (commonMQServerOverviewObj.message_stats.deliver_get_details) {
                msgRateChartOption.series[2].data.shift();
                msgRateChartOption.series[2].data.push(commonMQServerOverviewObj.message_stats.deliver_get_details.rate);
            }

            if (commonMQServerOverviewObj.message_stats.deliver_details) {
                msgRateChartOption.series[3].data.shift();
                msgRateChartOption.series[3].data.push(commonMQServerOverviewObj.message_stats.deliver_details.rate);
            }

            if (commonMQServerOverviewObj.message_stats.get_details) {
                msgRateChartOption.series[4].data.shift();
                msgRateChartOption.series[4].data.push(commonMQServerOverviewObj.message_stats.get_details.rate);
            }

            if (commonMQServerOverviewObj.message_stats.deliver_no_ack) {
                msgRateChartOption.series[5].data.shift();
                msgRateChartOption.series[5].data.push(commonMQServerOverviewObj.message_stats.deliver_no_ack.rate);
            }

            msgRateChartObj.setOption(msgRateChartOption);
        }
    }

</script>
