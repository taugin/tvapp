package com.android.tvapp.info;

public class PollInfo {
    public String taskId;
    public boolean logtofile;
    public boolean status;

    public String toString() {
        String out = "taskId : " + taskId + " , logtofile : " + logtofile + " , status : " + status;
        return out;
    }
}
