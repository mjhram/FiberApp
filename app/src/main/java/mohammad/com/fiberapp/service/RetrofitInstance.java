package mohammad.com.fiberapp.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {
    private static Retrofit retrofit=null;
    private static String BASE_URL="http://ec2-3-133-86-181.us-east-2.compute.amazonaws.com:1880/";

    public static GetKmzDataService getService() {


        if (retrofit == null){
            retrofit = new Retrofit
                    .Builder()
                    .baseUrl(BASE_URL)
                    //.addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())

                    .build();
        }
        return retrofit.create(GetKmzDataService.class);
    }
}
