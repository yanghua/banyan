<div id="nodeTableContainer"></div>
<div id="popNodeLayer" class="ltr" style="display: none; width: 540px; height: 300px">
    <form id="nodeForm" name="nodeForm" class="wufoo leftLabel page" accept-charset="UTF-8" autocomplete="off"
          enctype="multipart/form-data" novalidate>
        <ul>
            <li id="foli101" class="notranslate ">
                <label class="desc" id="title113" for="Field113">
                    节点名称
                </label>
                <div>
                    <input id="input_node_name" name="node.name" type="text" class="field text large" value="" maxlength="255"
                           tabindex="11" onkeyup=""/>
                </div>
            </li>
            <li id="foli103" class="notranslate ">
                <label class="desc" id="title110" for="Field113">
                    节点类型
                </label>
                <div>
                    <select id="input_node_type" name="node.type" class="field select large" tabindex="6">
                        <option value="0" selected="selected">
                            Exchange
                        </option>
                        <option value="1">
                            Queue
                        </option>
                    </select>
                </div>
            </li>
            <li id="foli105" class="notranslate ">
                <label class="desc" id="title110" for="Field113">
                    路由类型
                </label>
                <div>
                    <select id="select_node_routerType" name="node.routerType" class="field select large" tabindex="6">
                        <option value="0" selected="selected">
                            direct
                        </option>
                        <option value="1">
                            fanout
                        </option>
                        <option value="2">
                            topic
                        </option>
                        <option value="3">
                            header
                        </option>
                    </select>
                </div>
            </li>
            <li id="foli104" class="notranslate ">
                <label class="desc" id="title110" for="Field110">
                    父节点
                </label>
                <div>
                    <select id="select_node_parentId" name="node.parentId" class="field select large" tabindex="6">
                        <option value="-1" selected="selected" level="-1">
                            请选择当前节点要挂载的父节点
                        </option>
                        <#if nodeList?exists >
                            <#list nodeList as node>
                                <option value="${node.generatedId}" level="${node.level}">
                                ${node.name}
                                </option>
                            </#list>
                        </#if>
                    </select>
                </div>
            </li>
            <li id="foli105" style="display: none">
                <input type="hidden" id="input_node_level" name="node.level" value="-1" />
            </li>
            <li class="buttons ">
                <div>
                    <input id="saveForm" name="saveForm" class="btTxt submit"
                           type="button" value="提 交" />
                </div>
            </li>
            <li id="desc" style="margin-top: 60px;" >
                <label id="tip"></label>
            </li>
        </ul>
    </form>
</div>
<#include "/WEB-INF/widget/resource/jtable.ftl" />
<#include "/WEB-INF/widget/resource/wufoo.ftl" />
<script src="/resources/js/maintain/node.js" ></script>