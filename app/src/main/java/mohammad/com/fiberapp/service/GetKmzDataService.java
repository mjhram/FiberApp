package mohammad.com.fiberapp.service;

import mohammad.com.fiberapp.model.FileList;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetKmzDataService {
    @GET("list")
    Call<FileList> getResults();

    @GET("kmz")
    Call<ResponseBody> downloadFile(@Query("fn") String fname);
}
