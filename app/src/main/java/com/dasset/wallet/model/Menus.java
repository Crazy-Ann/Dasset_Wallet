package com.dasset.wallet.model;

import android.os.Parcel;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.BuildConfig;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.components.widget.sticky.listener.OnGroupListener;
import com.dasset.wallet.constant.ParameterKey;
import com.google.common.collect.Lists;

import java.util.List;


public class Menus extends BaseEntity implements OnGroupListener {

    private List<Menu> menus;

    public List<Menu> getMenus() {
        return menus;
    }

    public Menus parse(JSONObject object) {
        if (object != null) {
            if (object.containsKey(ParameterKey.Menu.Menu)) {
                JSONArray jsonArray = object.getJSONArray(ParameterKey.Menu.Menu);
                this.menus = Lists.newArrayList();
                for (int i = 0; i < jsonArray.size(); i++) {
                    this.menus.add(new Menu().parse(jsonArray.getJSONObject(i)));
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
            return "Menus{" +
                    "menus='" + menus + '\'' +
                    '}';
        } else {
            return super.toString();
        }
    }

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
        dest.writeTypedList(this.menus);
    }

    public Menus() {}

    protected Menus(Parcel in) {
        super(in);
        this.menus = in.createTypedArrayList(Menu.CREATOR);
    }

    public static final Creator<Menus> CREATOR = new Creator<Menus>() {
        @Override
        public Menus createFromParcel(Parcel source) {return new Menus(source);}

        @Override
        public Menus[] newArray(int size) {return new Menus[size];}
    };
}
