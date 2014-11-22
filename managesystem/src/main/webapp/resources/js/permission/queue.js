var PERMISSION_QUEUE = {
    isSendPermissionJTableInited: false,
    isInnerPermissionJTableInited: false
};

$(document).ready(function () {
    PERMISSION_QUEUE.initJTable();

    $('#queueTableContainer').jtable('load');

    PERMISSION_QUEUE.registerEvents();
});

PERMISSION_QUEUE.initJTable = function () {
    $('#queueTableContainer').jtable({
        title: '队列列表',
        paging: true,
        pageSize: 10,
        actions: {
            listAction: '/permission/queue/list'
        },
        fields: {
            nodeId: {
                title: '编号',
                key: true,
                list: false
            },
            name: {
                title: '节点名称',
                width: '48%'
            },
            value: {
                title: '内部名称',
                width: '48%'
            },
            sendPermission: {
                width: '2%',
                display: function (data) {
                    if (!data.record.inner) {
                        if (data.record.name.lastIndexOf("pubsub") != -1) {
                            return $('<img src="/resources/image/send_unable.png" disable="disabled" title="失效" class="clickStyle"  />');
                        } else {
                            //send permission
                            var sendPermBtn = $('<img src="/resources/image/send.png" title="发送授权" class="clickStyle"  />');
                            sendPermBtn.click(function () {
                                $('#selectedTargetId').val(data.record.nodeId);

                                if (!PERMISSION_QUEUE.isSendPermissionJTableInited) {
                                    PERMISSION_QUEUE.initSendPermNodeJTable();
                                    $('#sendPermNodeTableContainer').jtable('load');
                                    PERMISSION_QUEUE.isSendPermissionJTableInited = true;
                                } else {
                                    $('#sendPermNodeTableContainer').jtable('reload');
                                }

                                PERMISSION_QUEUE.popSendPermissionNodeLayer();
                            });

                            return sendPermBtn;
                        }
                    }
                }
            },
            receivePermission: {
                width: '2%',
                display: function (data) {
                    if (!data.record.inner) {
                        //in permission
                        var receivePermBtn;
                        receivePermBtn = $('<img src="/resources/image/receive.png" class="clickStyle" title="接收授权" alt="接收授权" />');
                        receivePermBtn.click(function () {
                            $('#selectedTargetId').val(data.record.nodeId);

                            if (!PERMISSION_QUEUE.isInnerPermissionJTableInited) {
                                PERMISSION_QUEUE.initReceivePermNodeJTable();
                                $('#receivePermNodeTableContainer').jtable('load');
                                PERMISSION_QUEUE.isReceivePermissionJTableInited = true;
                            } else {
                                $('#receivePermNodeTableContainer').jtable('reload');
                            }

                            PERMISSION_QUEUE.popReceivePermissionNodeLayer();
                        });

                        return receivePermBtn;
                    }
                }
            }
        }
    });
};

PERMISSION_QUEUE.popSendPermissionNodeLayer = function () {
    $.layer({
        type: 1,
        title: '发送节点列表',
        border: [1],
        shadeClose: false,
        area: ['700px', '600px'],
        page: {dom: '#sendPermissionLayer'},
        close: function () {
            PERMISSION_QUEUE.clearState();
        }
    });
};

PERMISSION_QUEUE.popReceivePermissionNodeLayer = function () {
    $.layer({
        type: 1,
        title: '接收节点列表',
        border: [1],
        shadeClose: false,
        area: ['700px', '600px'],
        page: {dom: '#receivePermissionLayer'},
        close: function () {
            PERMISSION_QUEUE.clearState();
        }
    });
};

PERMISSION_QUEUE.initSendPermNodeJTable = function () {
    $('#sendPermNodeTableContainer').jtable({
        title: '节点列表',
        selecting: true,
        multiselect: true,
        selectingCheckboxes: true,
        actions: {
            listAction: function (postData, jtParams) {
                return $.Deferred(function ($dfd) {
                    $.ajax({
                        url: '/permission/queue/sendlist?nodeid=' + $('#selectedTargetId').val(),
                        type: 'POST',
                        dataType: 'json',
                        data: postData,
                        success: function (data) {
                            $dfd.resolve(data);
                        },
                        error: function () {
                            $dfd.reject();
                        }
                    });
                });
            }
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
            }
        },
        selectionChanged: function () {
            //Get all selected rows
            var $selectedRows = $('#sendPermNodeTableContainer').jtable('selectedRows');
            var selectedGrantIdsJQObj = $('#selectedGrantIds');

            selectedGrantIdsJQObj.empty();
            if ($selectedRows.length > 0) {
                var tmp = "";
                $selectedRows.each(function () {
                    var record = $(this).data('record');
                    tmp += (record.nodeId + ",");
                });
            }

            selectedGrantIdsJQObj.val(tmp);
        },
        recordsLoaded: function (event, data) {
            $('#originalGrantIds').val(data.serverResponse.Others.join(','));
            var selectedRows = data.serverResponse.Others.map(function (id) {
                return $('#sendPermNodeTableContainer').jtable('getRowByKey', id)[0];
            });

            $('#sendPermNodeTableContainer').jtable('selectRows', $(selectedRows));
        }
    });

};

PERMISSION_QUEUE.initReceivePermNodeJTable = function () {
    $('#receivePermNodeTableContainer').jtable({
        title: '节点列表',
        selecting: true,
        multiselect: true,
        selectingCheckboxes: true,
        actions: {
            listAction: function (postData, jtParams) {
                return $.Deferred(function ($dfd) {
                    $.ajax({
                        url: '/permission/queue/receivelist?nodeid=' + $('#selectedTargetId').val(),
                        type: 'POST',
                        dataType: 'json',
                        data: postData,
                        success: function (data) {
                            $dfd.resolve(data);
                        },
                        error: function () {
                            $dfd.reject();
                        }
                    });
                });
            }
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
            }
        },
        selectionChanged: function () {
            //Get all selected rows
            var $selectedRows = $('#receivePermNodeTableContainer').jtable('selectedRows');

            var selectedGrantIdsJQObj = $('#selectedGrantIds');

            selectedGrantIdsJQObj.empty();
            if ($selectedRows.length > 0) {
                var tmp = "";
                $selectedRows.each(function () {
                    var record = $(this).data('record');
                    tmp += (record.nodeId + ",");
                });
            }

            selectedGrantIdsJQObj.val(tmp);
        },
        recordsLoaded: function (event, data) {
            $('#originalGrantIds').val(data.serverResponse.Others.join(','));
            var selectedRows = data.serverResponse.Others.map(function (id) {
                return $('#receivePermNodeTableContainer').jtable('getRowByKey', id)[0];
            });

            $('#receivePermNodeTableContainer').jtable('selectRows', $(selectedRows));
        }
    });

};

PERMISSION_QUEUE.registerEvents = function () {
    $('#sendPermissionBtn').click(function () {
        PERMISSION_QUEUE.authSendPermission();
    });

    $('#receivePermissionBtn').click(function () {
        PERMISSION_QUEUE.authReceivePermission();
    });
};

PERMISSION_QUEUE.authCommonProcess = function (url) {
    var targetId = $('#selectedTargetId').val();

    if (!targetId) {
        layer.alert('请选择需要授权的队列!', 8);
        return;
    }

    $.ajax({
        url: url,
        type: 'POST',
        async: false,
        data: {
            grantIds: $('#selectedGrantIds').val(),
            targetId: targetId,
            originalGrantIds: $('#originalGrantIds').val()
        },
        success: function (data) {
            if (data.Result === 'OK') {
                layer.alert('授权成功!');
            } else {
                layer.alert('授权失败!');
            }
        },
        error: function (err) {
            layer.alert('授权失败!', 8);
        }
    });
};

PERMISSION_QUEUE.authSendPermission = function () {
    PERMISSION_QUEUE.authCommonProcess("/permission/queue/sendpermission");
};

PERMISSION_QUEUE.authReceivePermission = function () {
    PERMISSION_QUEUE.authCommonProcess("/permission/queue/receivepermission");
};

PERMISSION_QUEUE.clearState = function () {
    $('#selectedTargetId').val('');
    $('#selectedGrantIds').val('');
    $('#originalGrantIds').val('');
    $('.jtable-row-selected').removeClass('jtable-row-selected');
};
