package com.hrrock.snapbook.interfaces;

import com.hrrock.snapbook.models.UserDetailsModel;
import com.hrrock.snapbook.models.YouNotificationModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by hp-u on 2/20/2018.
 */

public interface RetrofitApiInterface {
    @GET("CredentialController/doSignup")
    Call<String> signupDetails(@Query("email") String email, @Query("password") String password, @Query("name") String name
            , @Query("mobileno") String mobileno, @Query("username") String username, @Query("dpname") String dpname, @Query("gender") String gender);

    @GET("CredentialController/doLogin")
    Call<List<UserDetailsModel>> doLogin(@Query("id") String id, @Query("password") String password);
}
