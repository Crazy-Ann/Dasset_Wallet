package com.dasset.wallet.model;

import android.os.Parcel;

import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.BuildConfig;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.constant.ParameterKey;

public class Page extends BaseEntity {

    private String name;
    private String index;
    private String iconUrl;
    private String type;
    private String action;
    private String extendedParameter;

    public String getName() {
        return name;
    }

    public String getIndex() {
        return index;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getType() {
        return type;
    }

    public String getAction() {
        return action;
    }

    public String getExtendedParameter() {
        return extendedParameter;
    }

    public Page() {}

    public Page parse(JSONObject object) {
        if (object != null) {
            this.name = object.getString(ParameterKey.GetVersion.NAME);
            this.index = object.getString(ParameterKey.GetVersion.INDEX);
            this.iconUrl = object.getString(ParameterKey.GetVersion.ICON_URL);
            this.type = object.getString(ParameterKey.GetVersion.TYPE);
            this.action = object.getString(ParameterKey.GetVersion.ACTION);
            this.extendedParameter = object.getString(ParameterKey.GetVersion.EXTENDED_PARAMETER);
            return this;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "Page{" +
                    "name='" + name + '\'' +
                    ", index='" + index + '\'' +
                    ", iconUrl='" + iconUrl + '\'' +
                    ", type='" + type + '\'' +
                    ", action='" + action + '\'' +
                    ", extendedParameter='" + extendedParameter + '\'' +
                    '}';
        } else {
            return super.toString();
        }
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.name);
        dest.writeString(this.index);
        dest.writeString(this.iconUrl);
        dest.writeString(this.type);
        dest.writeString(this.action);
        dest.writeString(this.extendedParameter);
    }

    protected Page(Parcel in) {
        super(in);
        this.name = in.readString();
        this.index = in.readString();
        this.iconUrl = in.readString();
        this.type = in.readString();
        this.action = in.readString();
        this.extendedParameter = in.readString();
    }

    public static final Creator<Page> CREATOR = new Creator<Page>() {
        @Override
        public Page createFromParcel(Parcel source) {return new Page(source);}

        @Override
        public Page[] newArray(int size) {return new Page[size];}
    };
}
