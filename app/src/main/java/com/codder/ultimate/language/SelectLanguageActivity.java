package com.codder.ultimate.language;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.activity.SplashActivity;
import com.codder.ultimate.databinding.ActivitySelectLanguageBinding;
import com.codder.ultimate.launguagetranslation.TranslationManager;
import com.codder.ultimate.launguagetranslation.modelclass.ActiveLanguageRoot;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectLanguageActivity extends BaseActivity implements LanguageAdapter.OnLanguageClick {

    private ActivitySelectLanguageBinding binding;
    private SessionManager sessionManager;
    private LanguageAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectLanguageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new SessionManager(this);

        initViews();
        fetchLaunguages();
    }

    private void initViews() {
        adapter = new LanguageAdapter(
                this,
                new ArrayList<>(),
                this,
                getSavedDisplayKey()
        );
        binding.rvLanguages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLanguages.setAdapter(adapter);
    }

    private String getSavedDisplayKey() {
        String saved = sessionManager.getStringValue(Const.LANGUAGE);
        return TextUtils.isEmpty(saved) ? Const.ENGLISH : saved;
    }

    public void fetchLaunguages() {
        Call<ActiveLanguageRoot> call = RetrofitBuilder.create().fetchLanguage(1, 50);
        call.enqueue(new Callback<ActiveLanguageRoot>() {
            @Override
            public void onResponse(Call<ActiveLanguageRoot> call,
                                   Response<ActiveLanguageRoot> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isStatus()
                        && response.body().getDocs() != null) {

                    String defaultKey = Const.ENGLISH; // fallback

                    for (ActiveLanguageRoot.DocsItem doc : response.body().getDocs()) {
                        if (doc.isIsDefault()) {
                            defaultKey = doc.getLanguageTitle();
                            break;
                        }
                    }

                    String savedKey = sessionManager.getStringValue(Const.LANGUAGE);
                    String selectedKey = TextUtils.isEmpty(savedKey) ? defaultKey : savedKey;

                    adapter.updateData(response.body().getDocs(), selectedKey);
                }
            }

            @Override
            public void onFailure(Call<ActiveLanguageRoot> call, Throwable throwable) {

            }
        });
    }

    @Override
    public void onLanguageSelected(ActiveLanguageRoot.DocsItem item) {
        sessionManager.saveStringValue(Const.SELECTED_LANGUAGE, item.getLanguageCode()); // "en","hi"
        sessionManager.saveStringValue(Const.LANGUAGE, item.getLanguageTitle());          // "English","Hindi"

        adapter.setSelectedKey(item.getLanguageTitle());
        TranslationManager.getInstance().clearCache(this);
        showRestartPopup();
    }

    private void showRestartPopup() {
        new PopupBuilder(this).showSimplePopup(
                getString(R.string.restart_the_app),
                getString(R.string.restart),
                () -> {
                    Intent i = new Intent(SelectLanguageActivity.this, SplashActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
        );
    }
}