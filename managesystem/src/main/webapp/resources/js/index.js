$(document).ready(function () {

    //polling memory info
    pollingMem();

    //polling disk info
    pollingDisk();
});


var pollingMem = function () {
    var loopMemInfo = setInterval(memRefresh, 5000);
};

var pollingDisk = function () {
    var loopDiskInfo = setInterval(diskRefresh, 5000);
};


var memRefresh = function () {
    var memoryInfo = getMemInfo();
    resetMemChartOption(memoryInfo);
};

var diskRefresh = function () {
    var diskInfo = getDiskInfo();
    resetDiskChartOption(diskInfo);
};

var resetMemChartOption = function (memoryInfo) {
    //reset
};

var resetDiskChartOption = function (diskInfo) {
    //reset
};

var getMemInfo = function () {
    $.ajax({
        type    : 'GET',
        url     : '/Rabbitmq_overview.action',
        cache   : false,
        async   : false,
        success : function (response) {
            return response;
        },
        error   : function (error) {
            alert(error);
        }
    });
};

var getDiskInfo = function () {
    $.ajax({
        type    : 'GET',
        url     : 'Rabbitmq_overview.action',
        cache   : false,
        async   : false,
        success : function (response) {

        },
        error   : function (error) {

        }
    });
};


