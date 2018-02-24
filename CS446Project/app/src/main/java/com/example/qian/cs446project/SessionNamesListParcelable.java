package com.example.qian.cs446project;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Qian on 2018-02-24.
 */

// Ke Qiao Chen: I based this class on
// http://www.vogella.com/tutorials/AndroidParcelable/article.html
public class SessionNamesListParcelable implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public SessionNamesListParcelable createFromParcel(Parcel in) {
            return new SessionNamesListParcelable(in);
        }

        public SessionNamesListParcelable[] newArray(int size) {
            return new SessionNamesListParcelable[size];
        }

    };

    private ArrayList<String> sessionNamesList = new ArrayList<>();

    public SessionNamesListParcelable(ArrayList<String> sessionNamesList) {
        this.sessionNamesList = sessionNamesList;
    }

    public SessionNamesListParcelable(Parcel in) {
        this.sessionNamesList = in.readArrayList(sessionNamesList.getClass().getClassLoader());
    }

    public ArrayList<String> getSessionNamesList() {
        return sessionNamesList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(sessionNamesList);
    }

}
