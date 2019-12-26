package mohammad.com.fiberapp.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FileInfo {
    @SerializedName("fn")
    @Expose
    private String fn;
    @SerializedName("dt")
    @Expose
    private String dt;

    public String getFn() {
        return fn;
    }

    public void setFn(String fn) {
        this.fn = fn;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

}