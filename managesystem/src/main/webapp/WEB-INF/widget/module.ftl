<div class="top"><img class="logo" src="../../resources/image/logo.jpg"/>
    <ul class="nav">
        <#list firstLevelModules as flm>
            <li id="${flm.moduleCode}" value="${flm.moduleValue}"><a href="#">${flm.moduleName}</a></li>
        </#list>
    </ul>
</div>