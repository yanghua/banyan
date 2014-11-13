var ALARM_QUEUE = {};

$(document).ready(function () {
    ALARM_QUEUE.initJTable();

    $('#queueTableContainer').jtable('load');
});

ALARM_QUEUE.initJTable = function () {
    $('#queueTableContainer').jtable({
        title: '队列列表',
        paging: true,
        pageSize: 10,
        actions: {
            listAction: '/alarm/queue/list'
        },
        fields: {
            nodeId: {
                title: '编号',
                key: true,
                list: false
            },
            name: {
                title: '节点名称',
                width: '10%'
            },
            value: {
                title: '内部名称'
            },
            activate: {
                width: '2%',
                display: function (data) {
                    var availableBtn;
                    if (data.record.available) {
                        availableBtn = $('<img flag="1" alt="激活" src="/resources/image/enable.png" class="clickStyle" />');
                    } else {
                        availableBtn = $('<img flag="0" alt="禁用" src="/resources/image/unenable.png" class="clickStyle" />');
                    }

                    availableBtn.click(function () {
                        ALARM_QUEUE.activateNodeOrNot($(this), data.record.nodeId);
                    });

                    return availableBtn;
                }
            },
            reset: {
                width: '2%',
                display: function (data) {
                    //reset appid
                    var resetBtn;
                    resetBtn = $('<img alt="重置" class="clickStyle" src="/resources/image/reset.png" />');

                    resetBtn.click(function () {
                        ALARM_QUEUE.reset($(this), data.record);
                    });

                    return resetBtn;
                }
            }
        }
    });
};

ALARM_QUEUE.reset = function (jqObj, record) {
    $.ajax({
        url: '/alarm/node/reset',
        type: 'POST',
        data: {nodeId: record.nodeId},
        success: function (data) {
            if (data.Result == 'OK') {
                layer.alert('操作成功!', 8);
                $('#nodeTableContainer').jtable('reload');
            } else {
                layer.alert('操作失败!', 8);
            }
        },
        error: function (err) {
            layer.alert('操作失败!', 8);
        }
    });
};

ALARM_QUEUE.activateNodeOrNot = function (jqObj, nodeId) {
    var isAvailable = (jqObj.attr('flag') == '1');

    var urlAction = isAvailable ? "unactivate" : "activate";
    var urlStr = "/alarm/node/" + urlAction;

    $.ajax({
        url: urlStr,
        type: 'POST',
        data: {'nodeId': nodeId},
        success: function (data) {
            if (data.Result === 'OK') {
                if (isAvailable) {
                    jqObj.attr('flag', '0');
                    jqObj.attr('src', '/resources/image/unenable.png');
                } else {
                    jqObj.attr('flag', '1');
                    jqObj.attr('src', '/resources/image/enable.png');
                }
            } else {
                layer.alert('操作失败!', 8);
            }
        },
        error: function (err) {
            layer.alert('操作失败!', 8);
        }
    });
};