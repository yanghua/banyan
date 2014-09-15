<div id="submenuDiv" class="currmenu">
    <ul class="rig_nav">
    <#list secondLevelModules as slm >
        <li class="rig_seleli" style="display: none;" id="${slm.moduleCode}" parentId="${slm.parentModule}">
            <a href="${slm.linkUrl}">${slm.moduleName}</a>
        </li>
    </#list>
    </ul>
</div>