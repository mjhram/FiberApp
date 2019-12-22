package mohammad.com.fiberapp.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FileList {

    @SerializedName("files")
    @Expose
    private List<String> files = null;

    @SerializedName("fdates")
    @Expose
    private List<Long> fdates = null;

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<Long> getFdates() {
        return fdates;
    }

    public void setFdates(List<Long> fdates) {
        this.fdates = fdates;
    }

    public int compare(FileList fl) {
        if(fl==null) return 0;
        if(fl.getFiles().size()==0 || fl.getFdates().size() ==0) return 0;
        if(fl.getFiles().size() != fl.getFdates().size()) return 0;
        if(fl.getFiles().size() !=files.size()) return 0;
        if(fl.getFdates().size() !=fdates.size()) return 0;
        for(int i=0; i<files.size(); i++){
            int idx=fl.getFiles().indexOf(files.get(i));
            if(idx==-1) {
                return 0;
            }
            if(fl.getFdates().get(idx) != fdates.get(i)) return 0;
        }
        return 1;
    }
}
