package com.codder.ultimate.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.io.File;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateComplainViewModel extends AndroidViewModel {
    private static final String TAG = "CreateComplainViewModel";
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<RestResponse> complainResponse = new MutableLiveData<>();

    public CreateComplainViewModel(Application application) {
        super(application);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<RestResponse> getComplainResponse() {
        return complainResponse;
    }

    public void submitComplain(String message, String contact, String userId, File imageFile) {
        if (message == null || message.isEmpty()) {
            Log.e(TAG, "submitComplain: Message cannot be empty.");
            errorMessage.setValue("Message cannot be empty.");
            return;
        }

        isLoading.setValue(true);
        RequestBody messageBody = RequestBody.create(MediaType.parse("text/plain"), message);
        RequestBody contactBody = RequestBody.create(MediaType.parse("text/plain"), contact);
        RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), userId);

        MultipartBody.Part body = null;
        if (imageFile != null) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
            body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
        }

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("contact", contactBody);
        map.put("message", messageBody);
        map.put("userId", userIdBody);

        Call<RestResponse> call = RetrofitBuilder.create().addSupport(map, body);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    complainResponse.setValue(response.body());
                    Log.d(TAG, "submitComplain: Complaint submitted successfully.");
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Something went wrong";
                    errorMessage.setValue(errorMsg);
                    Log.w(TAG, "submitComplain: Response failed. Error: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getLocalizedMessage());
                Log.e(TAG, "submitComplain: Network failure", t);
            }
        });
    }
}

