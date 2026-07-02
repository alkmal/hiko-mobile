package com.codder.ultimate.launguagetranslation;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.launguagetranslation.modelclass.LatestCodeofLanguage;
import com.codder.ultimate.launguagetranslation.modelclass.SingleLanguageTranslation;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TranslationManager {

    private static TranslationManager instance;
    private Map<String, String> translations;
    private final Gson gson = new Gson();

    private TranslationManager() {}

    public static TranslationManager getInstance() {
        if (instance == null) {
            instance = new TranslationManager();
        }
        return instance;
    }

    /**
     * Translation fetch — version check karīne API call karo
     */
    public void fetchTranslations(Context context, String languageCode, Runnable onComplete) {

        String cachedVersion = getCachedTranslationVersion(context);
        boolean hasCachedData = hasCacheAvailable(context);

        // ✅ Cache chhe AND same language chhe — API call skip karo
        if (hasCachedData && !cachedVersion.isEmpty()) {
            loadFromCache(context);
            Log.d("TranslationManager", "Cache available — API skip ✅");
            if (onComplete != null) onComplete.run();
            return; // ← API call j nahi thay
        }

        // Cache nathi ya version different chhe — API call karo
        Call<SingleLanguageTranslation> call = RetrofitBuilder.create()
                .fetchTranslation(languageCode, "app");

        call.enqueue(new Callback<SingleLanguageTranslation>() {
            @Override
            public void onResponse(Call<SingleLanguageTranslation> call,
                                   Response<SingleLanguageTranslation> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isStatus()
                        && response.body().getDoc() != null
                        && response.body().getDoc().getTranslations() != null) {

                    String apiVersion = response.body().getVersion();
                    translations = response.body().getDoc().getTranslations();
                    saveToCache(context, languageCode, translations, apiVersion);
                    Log.d("TranslationManager", "Fresh data cached — version: " + apiVersion + " ✅");

                } else {
                    loadFromCache(context);
                }

                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onFailure(Call<SingleLanguageTranslation> call, Throwable throwable) {
                loadFromCache(context);
                if (onComplete != null) onComplete.run();
            }
        });
    }

    // ✅ New helper
    private boolean hasCacheAvailable(Context ctx) {
        String json = ctx.getSharedPreferences(Const.PREF_TRANSLATIONS, Context.MODE_PRIVATE)
                .getString(Const.KEY_TRANSLATIONS_JSON, null);
        return json != null && !json.isEmpty();
    }

//    public void checkAndUpdateGlobalVersion(Context context, Runnable onComplete) {
//
//        String cachedGlobalVersion = getCachedGlobalVersion(context); // ← String
//
//        Call<LatestCodeofLanguage> call = RetrofitBuilder.create().fetchGlobalVersion();
//
//        call.enqueue(new Callback<LatestCodeofLanguage>() {
//            @Override
//            public void onResponse(Call<LatestCodeofLanguage> call,
//                                   Response<LatestCodeofLanguage> response) {
//
//                if (response.isSuccessful() && response.body() != null) {
//
//                    // ✅ String version — parseInt nahi
//                    String apiGlobalVersion = response.body().getData();
//
//                    if (!apiGlobalVersion.equals(cachedGlobalVersion)) {
//                        Log.d("TranslationManager", "Global version updated: " + apiGlobalVersion);
//                        clearCache(context);
//                        saveGlobalVersion(context, apiGlobalVersion);
//
//                        String langCode = context.getSharedPreferences("session", Context.MODE_PRIVATE)
//                                .getString(Const.SELECTED_LANGUAGE, "en");
//                        fetchTranslations(context, langCode, onComplete);
//
//                    } else {
//                        Log.d("TranslationManager", "Global version same — skip");
//                        if (onComplete != null) onComplete.run();
//                    }
//                } else {
//                    if (onComplete != null) onComplete.run();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<LatestCodeofLanguage> call, Throwable throwable) {
//                if (onComplete != null) onComplete.run();
//            }
//        });
//    }


    public void checkAndUpdateGlobalVersion(Context context, Runnable onComplete) {

        String cachedGlobalVersion = getCachedGlobalVersion(context);

        Call<LatestCodeofLanguage> call = RetrofitBuilder.create().fetchGlobalVersion();

        call.enqueue(new Callback<LatestCodeofLanguage>() {
            @Override
            public void onResponse(Call<LatestCodeofLanguage> call,
                                   Response<LatestCodeofLanguage> response) {

                if (response.isSuccessful() && response.body() != null) {

                    String apiGlobalVersion = response.body().getData();

                    if (!apiGlobalVersion.equals(cachedGlobalVersion)) {
                        Log.d("TranslationManager", "Global version updated: " + apiGlobalVersion);
                        clearCache(context);
                        saveGlobalVersion(context, apiGlobalVersion);

                        // ✅ SessionManager thi correct language lo
                        String langCode = new SessionManager(context)
                                .getStringValue(Const.SELECTED_LANGUAGE);

                        // ✅ Empty hoy to onComplete directly — SplashActivity fetch karso
                        if (TextUtils.isEmpty(langCode)) {
                            Log.d("TranslationManager", "No language saved — skip fetch");
                            if (onComplete != null) onComplete.run();
                            return;
                        }

                        fetchTranslations(context, langCode, onComplete);

                    } else {
                        Log.d("TranslationManager", "Global version same — skip");
                        if (onComplete != null) onComplete.run();
                    }
                } else {
                    if (onComplete != null) onComplete.run();
                }
            }

            @Override
            public void onFailure(Call<LatestCodeofLanguage> call, Throwable throwable) {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    // ─── Cache helpers ────────────────────────────────────────────────

    private void saveToCache(Context ctx, String langCode,
                             Map<String, String> map, String version) { // ← String
        ctx.getSharedPreferences(Const.PREF_TRANSLATIONS, Context.MODE_PRIVATE)
                .edit()
                .putString(Const.KEY_TRANSLATIONS_JSON, gson.toJson(map))
                .putString(Const.KEY_TRANSLATIONS_LANG, langCode)
                .putString(Const.KEY_TRANSLATIONS_VERSION, version) // ← putString
                .apply();
    }

    private void loadFromCache(Context ctx) {
        String json = ctx.getSharedPreferences(Const.PREF_TRANSLATIONS, Context.MODE_PRIVATE)
                .getString(Const.KEY_TRANSLATIONS_JSON, null);
        if (json != null && !json.isEmpty()) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> loaded = gson.fromJson(json, type);
            if (loaded != null && !loaded.isEmpty()) {
                translations = loaded;
            }
        }
    }

    private String getCachedTranslationVersion(Context ctx) { // ← String return
        return ctx.getSharedPreferences(Const.PREF_TRANSLATIONS, Context.MODE_PRIVATE)
                .getString(Const.KEY_TRANSLATIONS_VERSION, ""); // ← getString
    }

    private void saveGlobalVersion(Context ctx, String version) { // ← String
        ctx.getSharedPreferences(Const.PREF_TRANSLATIONS, Context.MODE_PRIVATE)
                .edit()
                .putString(Const.KEY_GLOBAL_VERSION, version) // ← putString
                .apply();
    }


    private String getCachedGlobalVersion(Context ctx) { // ← String return
        return ctx.getSharedPreferences(Const.PREF_TRANSLATIONS, Context.MODE_PRIVATE)
                .getString(Const.KEY_GLOBAL_VERSION, ""); // ← getString
    }

    public void clearCache(Context context) {
        context.getSharedPreferences(Const.PREF_TRANSLATIONS, Context.MODE_PRIVATE)
                .edit().clear().apply();
        translations = null;
    }

    private String getSavedLanguageCode(Context ctx) {
        return ctx.getSharedPreferences(Const.PREF_TRANSLATIONS, Context.MODE_PRIVATE)
                .getString(Const.KEY_TRANSLATIONS_LANG, "");
    }

    public String get(String key) {
        if (translations != null && translations.containsKey(key)) {
            return translations.get(key);
        }
        return null;
    }
}