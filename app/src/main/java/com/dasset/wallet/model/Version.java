package com.dasset.wallet.model;

import android.os.Parcel;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.BuildConfig;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.constant.ParameterKey;
import com.google.common.collect.Lists;

import java.util.List;

public class Version extends BaseEntity {

    private String     clientVersion;
    private String     versionName;
    private String     downloadUrl;
    private String     lowestClientVersion;
    private String     apk;
    private String     updateMessage;
    private String     serviceUrl;
    private String     pageSrcSign;
    private List<Page> pagesList;

    public String getClientVersion() {
        return clientVersion;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getLowestClientVersion() {
        return lowestClientVersion;
    }

    public String getApk() {
        return apk;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getPageSrcSign() {
        return pageSrcSign;
    }

    public List<Page> getPagesList() {
        return pagesList;
    }

    @Override
    public Version parse(JSONObject object) {
        if (object != null) {
            this.clientVersion = object.getString(ParameterKey.GetVersion.CLIENT_VERSION);
            this.versionName = object.getString(ParameterKey.GetVersion.VERSION_NAME);
            this.downloadUrl = object.getString(ParameterKey.GetVersion.DOWNLOAD_URL);
            this.lowestClientVersion = object.getString(ParameterKey.GetVersion.LOWEST_CLIENT_VERSION);
            this.apk = object.getString(ParameterKey.GetVersion.APK);
            this.updateMessage = object.getString(ParameterKey.GetVersion.UPDATE_MESSAGE);
            this.serviceUrl = object.getString(ParameterKey.GetVersion.SERVICE_URL);
            this.pageSrcSign = object.getString(ParameterKey.GetVersion.PAGE_SRC_SIGN);
            if (object.containsKey(ParameterKey.GetVersion.PAGES_LIST)) {
                JSONArray jsonArray = object.getJSONArray(ParameterKey.GetVersion.PAGES_LIST);
                this.pagesList = Lists.newArrayList();
                for (int i = 0; i < jsonArray.size(); i++) {
                    this.pagesList.add(new Page().parse(jsonArray.getJSONObject(i)));
                }
            }
            return this;

        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "Version{" +
                    "clientVersion='" + clientVersion + '\'' +
                    ", versionName='" + versionName + '\'' +
                    ", downloadUrl='" + downloadUrl + '\'' +
                    ", lowestClientVersion='" + lowestClientVersion + '\'' +
                    ", apk='" + apk + '\'' +
                    ", updateMessage='" + updateMessage + '\'' +
                    ", serviceUrl='" + serviceUrl + '\'' +
                    ", pageSrcSign='" + pageSrcSign + '\'' +
                    ", pagesList='" + pagesList + '\'' +
                    '}';
        } else {
            return super.toString();
        }
    }

    public Version() {}

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.clientVersion);
        dest.writeString(this.versionName);
        dest.writeString(this.downloadUrl);
        dest.writeString(this.lowestClientVersion);
        dest.writeString(this.apk);
        dest.writeString(this.updateMessage);
        dest.writeString(this.serviceUrl);
        dest.writeString(this.pageSrcSign);
        dest.writeTypedList(this.pagesList);
    }

    protected Version(Parcel in) {
        super(in);
        this.clientVersion = in.readString();
        this.versionName = in.readString();
        this.downloadUrl = in.readString();
        this.lowestClientVersion = in.readString();
        this.apk = in.readString();
        this.updateMessage = in.readString();
        this.serviceUrl = in.readString();
        this.pageSrcSign = in.readString();
        this.pagesList = in.createTypedArrayList(Page.CREATOR);
    }

    public static final Creator<Version> CREATOR = new Creator<Version>() {
        @Override
        public Version createFromParcel(Parcel source) {return new Version(source);}

        @Override
        public Version[] newArray(int size) {return new Version[size];}
    };
}
