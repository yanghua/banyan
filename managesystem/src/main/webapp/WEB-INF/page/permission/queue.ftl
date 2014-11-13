<div id="queueTableContainer"></div>
<!-- send permission -->
<div id="sendPermissionLayer" class="ltr" style=" display: none; width: 700px; height: 600px;">
    <div id="sendPermNodeTableContainer" style="width: 100%; height: 85%; overflow-y: scroll;"></div>
    <div style="height: 10%; text-align: center;">
        <input style="margin-top: 20px;" value="发送授权" type="button" id="sendPermissionBtn" />
    </div>
</div>
<!-- receive permission -->
<div id="receivePermissionLayer" class="ltr" style="display: none; widows: 700px; height: 600px;">
    <div id="receivePermNodeTableContainer" style="width: 100%; height: 85%; overflow-y: scroll;"></div>
    <div style="height: 10%; text-align: center;">
        <input style="margin-top: 20px;" value="接收授权" type="button" id="receivePermissionBtn" />
    </div>
</div>
<input id="selectedTargetId" name="selectedTargetId" type="hidden" />
<input id="selectedGrantIds" name="selectedGrantIds" type="hidden"/>
<input id="originalGrantIds" name="originalGrantIds" type="hidden" />
<#include "/WEB-INF/widget/resource/jtable.ftl" />
<script src="/resources/js/permission/queue.js" ></script>