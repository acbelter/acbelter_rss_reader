package com.acbelter.rssreader.core;

import android.os.Parcel;
import android.os.Parcelable;

public class RSSChannel implements Parcelable {
    protected String mRssLink;
    protected String mTitle;
    protected String mDescription;
    protected String mLink;

    public RSSChannel() {
        mRssLink = "";
        mTitle = "";
        mDescription = "";
        mLink = "";
    }

    private RSSChannel(Parcel in) {
        mRssLink = in.readString();
        mTitle = in.readString();
        mDescription = in.readString();
        mLink = in.readString();
    }

    public String getRssLink() {
        return mRssLink;
    }

    public void setRssLink(String rssLink) {
        mRssLink = rssLink;
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

    public static final Parcelable.Creator<RSSChannel> CREATOR =
            new Parcelable.Creator<RSSChannel>() {
                @Override
                public RSSChannel createFromParcel(Parcel in) {
                    return new RSSChannel(in);
                }

                @Override
                public RSSChannel[] newArray(int size) {
                    return new RSSChannel[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mRssLink);
        out.writeString(mTitle);
        out.writeString(mDescription);
        out.writeString(mLink);
    }
    @Override
    public String toString() {
        return "RSSChannel{" +
                "mRssLink='" + mRssLink + '\'' +
                ", mTitle='" + mTitle + '\'' +
                '}';
    }
}
