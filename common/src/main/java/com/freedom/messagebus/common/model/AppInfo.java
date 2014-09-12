package com.freedom.messagebus.common.model;

/**
 * app info
 */
public class AppInfo extends BaseModel {

    private String appId;

    private short majorVersion;
    private short minorVersion;
    private short thirdVersion;

    private String accessUrl;
    private String desc;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public short getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(short majorVersion) {
        this.majorVersion = majorVersion;
    }

    public short getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(short minorVersion) {
        this.minorVersion = minorVersion;
    }

    public short getThirdVersion() {
        return thirdVersion;
    }

    public void setThirdVersion(short thirdVersion) {
        this.thirdVersion = thirdVersion;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppInfo appInfo = (AppInfo) o;

        if (majorVersion != appInfo.majorVersion) return false;
        if (minorVersion != appInfo.minorVersion) return false;
        if (thirdVersion != appInfo.thirdVersion) return false;
        if (!accessUrl.equals(appInfo.accessUrl)) return false;
        if (!appId.equals(appInfo.appId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appId.hashCode();
        result = 31 * result + (int) majorVersion;
        result = 31 * result + (int) minorVersion;
        result = 31 * result + (int) thirdVersion;
        return result;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
            "appId='" + appId + '\'' +
            ", majorVersion=" + majorVersion +
            ", minorVersion=" + minorVersion +
            ", thirdVersion=" + thirdVersion +
            ", accessUrl='" + accessUrl + '\'' +
            ", desc='" + desc + '\'' +
            '}';
    }
}
