$(document).ready(function () {
    registerFirstLevelMenuEvents();

    var currentUrl = window.location.pathname;
    //find current url's html tag
    var $currentSubmenu = null;
    $(".currmenu .rig_nav li").each(function (index) {
        if($(this).find('a').attr('href') === currentUrl) {
            $currentSubmenu = $(this);
        }
    });

    var parentModule = null;
    if(!$currentSubmenu) {
        return;
    }

    parentModule = $currentSubmenu.attr('parentid');

    if(!parentModule) {
        return;
    }

    showSubMenus(parentModule);
    var currentFirstLevelModule = $('#' + parentModule);
    highlightParentMenu(currentFirstLevelModule);
});

var registerFirstLevelMenuEvents = function () {
    $('.nav li').each(function () {
        $(this).mouseover(function () {
            var currentId = $(this).attr('id');
            if (currentId) {
                highlightParentMenu($(this));
                showSubMenus(currentId);
            }
        });
    })
};

var showSubMenus = function (parentId) {
    //show second level menu items
    $(".currmenu .rig_nav li").hide();
    $(".currmenu .rig_nav li[parentid='" + parentId + "']").each(function () {
        $(this).show();
    });
};

var highlightParentMenu = function ($parentMenuItem) {
    //clear all style
    $('.nav li').removeClass('seleli');
    $parentMenuItem.addClass('seleli');
};
