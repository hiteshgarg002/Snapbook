package com.hrrock.snapbook.networks;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by hp-u on 2/20/2018.
 */

public class RetrofitApiClient {
    private static String BASE_URL = "http://hoggish-divider.000webhostapp.com/Snapbook/index.php/";
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitForCredentialController() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}

