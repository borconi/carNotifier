package uk.co.emil.borconi.carnotifier;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Comparator;

public class PackageClass implements Comparable {


    String appName;
    String packageName;
    Boolean isEnabled;


    public PackageClass(String appName, String packageName, Boolean isEnabled) {
        this.appName = appName;
        this.packageName = packageName;
        this.isEnabled = isEnabled;
    }

    public String getAppName() {
        return appName;
    }

    @Override
    public String toString()
    {
        return appName;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        PackageClass prev = (PackageClass) o;
        return appName.toLowerCase().compareTo(prev.toString().toLowerCase());
    }
}

