package mohammad.com.fiberapp.service;

import java.util.List;

import io.reactivex.Observable;
import mohammad.com.fiberapp.model.FileInfo;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetKmzDataService {
    @GET("list")
    Observable<List<FileInfo>> getResults();

    @GET("kmz")
    Observable<ResponseBody> downloadFile(@Query("fn") String fname);
}
