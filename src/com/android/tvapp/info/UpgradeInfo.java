package com.android.tvapp.info;

public class UpgradeInfo {
    public String downloadurl;
    public String apkname;
    public String versioncode;
    public String versionname;
    public String releasetime;
    public String instructions;
    public String pkgname;
    public String apksize;

    public String toString() {
        String str = "\n";
        str += "appurl       : " + downloadurl + "\n";
        str += "appname      : " + apkname + "\n";
        str += "versioncode  : " + versioncode + "\n";
        str += "versionname  : " + versionname + "\n";
        str += "releasetime  : " + releasetime + "\n";
        str += "instructions : " + instructions + "\n";
        return str;
    }
}
