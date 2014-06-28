package com.acbelter.rssreader.core;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseRSSElement implements Parcelable {
    protected String mTitle;
    protected String mDescription;
    protected String mLink;

    public BaseRSSElement() {
        mTitle = "";
        mDescription = "";
        mLink = "";
    }

    private BaseRSSElement(Parcel in) {
        mTitle = in.readString();
        mDescription = in.readString();
        mLink = in.readString();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String link) {
        mLink = link;
    }

    public static final Parcelable.Creator<BaseRSSElement> CREATOR =
            new Parcelable.Creator<BaseRSSElement>() {
                @Override
                public BaseRSSElement createFromParcel(Parcel in) {
                    return new BaseRSSElement(in);
                }

                @Override
                public BaseRSSElement[] newArray(int size) {
                    return new BaseRSSElement[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mTitle);
        out.writeString(mDescription);
        out.writeString(mLink);
    }
}
