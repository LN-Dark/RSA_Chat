package com.luanegra.rsachat.fragments;

import com.luanegra.rsachat.notifications.MyResponse;
import com.luanegra.rsachat.notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAbgP8A60:APA91bEPo0Kamxi5BggxQ3leNV4WGNtAqQRLiumsAkXHyHu8oqSaR2yvlsEPB2L38mLJhoiQ3SEv1NJpyKN4SuclVxeSQw4kDKj5OqToNvmxfpfLI3j5ThN21j7Fw0Y1T4AFc-SfZS-B"})
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
