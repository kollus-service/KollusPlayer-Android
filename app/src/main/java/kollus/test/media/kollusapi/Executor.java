package kollus.test.media.kollusapi;

import kollus.test.media.constant.KollusConstant;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Executor {
    private static Executor _instance = new Executor();

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(KollusConstant.KOLLUS_API_URI)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private KollusApiService service = retrofit.create(KollusApiService.class);

    public static Executor getInstance() {
        return _instance;
    }

    private Executor() {
    }


    public KollusApiService getService() {
        return this.service;
    }

}
