package com.dasset.wallet.model;

import android.os.Parcel;

import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.BuildConfig;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.components.widget.sticky.listener.OnGroupListener;
import com.dasset.wallet.constant.ParameterKey;


public class Menu extends BaseEntity implements OnGroupListener {

    private String menuName;

    public String getMenuName() {
        return menuName;
    }

    public Menu parse(JSONObject object) {
        if (object != null) {
            this.menuName = object.getString(ParameterKey.Menu.NAME);
            return this;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "Menu{" +
                    "menuName='" + menuName + '\'' +
                    '}';
        } else {
            return super.toString();
        }
    }

    public Menu() {}

    @Override
    public String getGroupName() {
        return null;
    }

    @Override
    public long getGroupId() {
        return 0;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.menuName);
    }

    protected Menu(Parcel in) {
        super(in);
        this.menuName = in.readString();
    }

    public static final Creator<Menu> CREATOR = new Creator<Menu>() {
        @Override
        public Menu createFromParcel(Parcel source) {return new Menu(source);}

        @Override
        public Menu[] newArray(int size) {return new Menu[size];}
    };
}
