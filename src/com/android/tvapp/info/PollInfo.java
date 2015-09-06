package com.android.tvapp.info;

public class PollInfo {
    public String taskid;
    public boolean logtofile;
    
    public String toString() {
        String out = "taskId : " + taskid + " , logtofile : " + logtofile;
        return out;
    }
}
