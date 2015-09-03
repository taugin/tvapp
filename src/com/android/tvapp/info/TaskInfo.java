package com.android.tvapp.info;

public class TaskInfo {
    public String ip;
    public String name;
    public String type;
    public String audiourl;
    public String videourl;
    public String texturl;
    public String content;
    public String time;
    public String []imgurl;
    
    public String toString() {
        String out = "";
        out += "ip       : " + ip + "\n";
        out += "name     : " + name + "\n";
        out += "type     : " + type + "\n";
        out += "audiourl : " + audiourl + "\n";
        out += "videourl : " + videourl + "\n";
        out += "texturl  : " + texturl + "\n";
        out += "content  : " + content + "\n";
        out += "time     : " + time + "\n";
        if (imgurl != null) {
            for (String url : imgurl) {
                out += "imgurl   : " + url + "\n";
            }
        }
        return out;
    }
}
