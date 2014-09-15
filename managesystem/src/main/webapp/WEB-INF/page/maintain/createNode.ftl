<!-- CSS -->
<link href="/resources/component/form/css/structure.css" rel="stylesheet">
<link href="/resources/component/form/css/form.css" rel="stylesheet">
<!-- JavaScript -->
<script src="/resources/component/form/scripts/wufoo.js"></script>
<!--[if lt IE 10]>
<script src="https://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
<div id="container" class="ltr" style="position: absolute;">
    <form id="form1" name="form1" class="wufoo leftLabel page" accept-charset="UTF-8" autocomplete="off"
          enctype="multipart/form-data" method="post" novalidate
          action="/maintain/Topology/create">
        <ul>
            <li id="foli101" class="notranslate ">
                <label class="desc" id="title113" for="Field113">
                    节点名称
                </label>
                <div>
                    <input id="Field111" name="node.name" type="text" class="field text large" value="" maxlength="255"
                           tabindex="11" onkeyup=""/>
                </div>
            </li>
            <li id="foli102" class="notranslate ">
                <label class="desc" id="title113" for="Field113">
                    节点属性
                </label>
                <div>
                    <input id="Field112" name="node.value" type="text" class="field text large" value="" maxlength="255"
                           tabindex="11" onkeyup=""/>
                </div>
            </li>
            <li id="foli103" class="notranslate ">
                <label class="desc" id="title110" for="Field113">
                    节点类型
                </label>
                <div>
                    <select id="Field113" name="node.type" class="field select large" tabindex="6">
                        <option value="0" selected="selected">
                            Exchange
                        </option>
                        <option value="1">
                            Queue
                        </option>
                    </select>
                </div>
            </li>
            <li id="foli104" class="notranslate ">
                <label class="desc" id="title110" for="Field110">
                    父节点
                </label>
                <div>
                    <select id="Field110" name="node.parentId" class="field select large" tabindex="6">
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
                <input type="hidden" id="foli105_level" name="node.level" value="-1" />
            </li>
            <li class="buttons ">
                <div>
                    <input id="saveForm" name="saveForm" class="btTxt submit"
                           type="submit" value="Submit" onclick="return validateForm();"/>
                </div>
            </li>
            <li class="hide">
                <label for="comment">Do Not Fill This Out</label>
                <textarea name="comment" id="comment" rows="1" cols="1"></textarea>
                <input type="hidden" id="idstamp" name="idstamp" value="Ow2WrsyplWPQYVT9DwznFLSFvtOVjFPbMIBnwkvSF/A="/>
            </li>
        </ul>
    </form>
    <script>
        $(document).ready(function () {
            //select's option event
            $('#Field110').change(function () {
                var $selectedItem = $(this).children('option:selected');
                var selectedLevel = $selectedItem.attr('level');
                var intLevel = parseInt(selectedLevel);
                $('#foli105_level').val(intLevel + 1);
            });
        });

        var validateForm = function () {
            var result = validateLevel() && validateEmpty($('#Field111')) && validateEmpty($('#Field112'));
            if (!result) {
                alert("表单验证失败");
                return result;
            }

            return result;
        };

        var validateLevel = function () {
            var currentLevel = $('#foli105_level').val();
            if (currentLevel) {
                try {
                    var int_level = parseInt(currentLevel);
                    if (int_level === -1) {
                        alert("请先选择挂载的父节点后方可提交");
                        return false;
                    }

                    return true;
                } catch (e) {
                    return false;
                }
            } else {
                return false;
            }
        };

        var validateEmpty = function ($obj) {
            if (!$obj) {
                return false;
            }

            if (!$obj.val()) {
                return false;
            }

            return true;
        }
    </script>
</div>
