package com.messagebus.managesystem.pojo;

import java.io.Serializable;

public class Module implements Serializable {

    private String moduleCode;
    private String moduleName;
    private String moduleValue;
    private String linkUrl;
    private String parentModule;
    private int    sortIndex;
    private String remark;

    public Module() {
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleValue() {
        return moduleValue;
    }

    public void setModuleValue(String moduleValue) {
        this.moduleValue = moduleValue;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getParentModule() {
        return parentModule;
    }

    public void setParentModule(String parentModule) {
        this.parentModule = parentModule;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "Module{" +
            "moduleCode='" + moduleCode + '\'' +
            ", moduleName='" + moduleName + '\'' +
            ", moduleValue='" + moduleValue + '\'' +
            ", linkUrl='" + linkUrl + '\'' +
            ", parentModule='" + parentModule + '\'' +
            ", sortIndex=" + sortIndex +
            ", remark='" + remark + '\'' +
            '}';
    }
}
