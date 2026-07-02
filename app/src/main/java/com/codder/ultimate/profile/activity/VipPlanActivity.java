package com.codder.ultimate.profile.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.activity.FlutterwaveWebViewActivity;
import com.codder.ultimate.activity.SplashActivity;
import com.codder.ultimate.databinding.ActivityVipPlanBinding;
import com.codder.ultimate.databinding.BottomSheetPaymentBinding;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.profile.adapter.DotAdapter;
import com.codder.ultimate.profile.adapter.VipImagesAdapter;
import com.codder.ultimate.profile.adapter.VipPlanAdapter;
import com.codder.ultimate.profile.modelclass.CreateUserStripe;
import com.codder.ultimate.profile.modelclass.SettingRoot;
import com.codder.ultimate.profile.modelclass.VipPlanRoot;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonObject;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VipPlanActivity extends BaseActivity implements com.razorpay.PaymentResultWithDataListener{

    private static final String TAG = "VipPlanActivity";
    private static final String STR_STRIPE = "stripe";
    private static final String STR_GP = "google pay";

    private ActivityVipPlanBinding binding;
    private VipImagesAdapter vipImagesAdapter = new VipImagesAdapter();
    private VipPlanAdapter vipPlanAdapter = new VipPlanAdapter();
    private List<String> paymentGateways = new ArrayList<>();
    private VipPlanRoot.VipPlanItem selectedPlan;
    private BillingClient billingClient;
    private boolean apiCalled = false;
    private BottomSheetDialog bottomSheetDialog;
    private SettingRoot.Setting setting;
    private String country, currency, paymentGateway, productId, planId, paymentClientSecret, paymentIntent;
    private boolean isVip = true;
    private PaymentSheet paymentSheet;
    private PaymentSheet.CustomerConfiguration customerConfig;
    String selectedPlanId;

    private static final String STR_PAYPAL = "paypal";
    private static final String STR_RAZORPAY = "razorpay";
    private static final String STR_CASHFREE = "cashfree";
    private static final String STR_PAYSTACK = "paystack";

    private String PAYPAL_LIVE_CLIENT_ID = "";
    private String PAYPAL_LIVE_SECRET = "";
    private String paypalAccessToken = "";
    private String paypalOrderId = "";
    private OkHttpClient okHttpClient = new OkHttpClient();

    private String RAZORPAY_LIVE_KEY = "";

    private String CASHFREE_LIVE_APP_ID = "";
    private String CASHFREE_LIVE_SECRET = "";

    private String PAYSTACK_LIVE_SECRET_KEY = "";

    private androidx.activity.result.ActivityResultLauncher<Intent> paystackLauncher;

    private static final String STR_FLUTTERWAVE = "flutterwave";
    private String FLUTTERWAVE_SECRET_KEY = "";
    private androidx.activity.result.ActivityResultLauncher<Intent> flutterwaveLauncher;


    private final PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null && !purchases.isEmpty()) {
            runOnUiThread(() -> {
                customDialogClass.show();
                handlePurchase(purchases.get(0));
            });
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "Purchase canceled by user.");
        } else {
            Log.e(TAG, "Purchase failed with response code: " + billingResult.getResponseCode());
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Uri data = intent.getData();

        // ✅ VipPlan PayPal
        if (data != null && data.toString().contains("vippaypal")) {
            if (data.toString().contains("cancel")) {
                Toast.makeText(this, getString(R.string.purchase_canceled), Toast.LENGTH_SHORT).show();
                return;
            }
            String token = data.getQueryParameter("token");
            if (token != null && !token.isEmpty()) {
                paypalOrderId = token;
                capturePaypalOrder(token);
            } else if (paypalOrderId != null && !paypalOrderId.isEmpty()) {
                capturePaypalOrder(paypalOrderId);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vip_plan);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        // ✅ Live credentials
        PAYPAL_LIVE_CLIENT_ID = sessionManager.getSetting().getPaypalClientId();
        PAYPAL_LIVE_SECRET = sessionManager.getSetting().getPaypalSecretKey();
        RAZORPAY_LIVE_KEY = sessionManager.getSetting().getRazorSecretKey();
        CASHFREE_LIVE_APP_ID = sessionManager.getSetting().getCashfreeClientId();
        CASHFREE_LIVE_SECRET = sessionManager.getSetting().getCashfreeClientSecret();
        PAYSTACK_LIVE_SECRET_KEY = sessionManager.getSetting().getPaystackSecretKey();
        FLUTTERWAVE_SECRET_KEY = sessionManager.getSetting().getFlutterwaveSecretKey();

        // ✅ Paystack WebView launcher
        paystackLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == com.codder.ultimate.activity.PaystackWebViewActivity.RESULT_SUCCESS
                            && result.getData() != null) {
                        String ref = result.getData()
                                .getStringExtra(com.codder.ultimate.activity.PaystackWebViewActivity.EXTRA_REFERENCE);
                        verifyPaystackPayment(ref);
                    } else {
                        Toast.makeText(this, getString(R.string.purchase_canceled),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        flutterwaveLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == FlutterwaveWebViewActivity.RESULT_SUCCESS
                            && result.getData() != null) {
                        String ref = result.getData()
                                .getStringExtra(FlutterwaveWebViewActivity.EXTRA_REFERENCE);
                        verifyFlutterwavePayment(ref);
                    } else {
                        Toast.makeText(this, getString(R.string.purchase_canceled),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // ✅ RazorPay preload
//        com.razorpay.Checkout.preload(getApplicationContext());
//        // ✅ Paystack init
//        co.paystack.android.PaystackSdk.initialize(getApplicationContext());

        initViews();
        setupBillingClient();
    }

    private void initViews() {
        DotAdapter dotAdapter = new DotAdapter(vipImagesAdapter.getItemCount(), R.color.tintColor);
        binding.rvDots.setAdapter(dotAdapter);
        binding.rvBanner.setAdapter(vipImagesAdapter);
        binding.rvBanner.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager myLayoutManager = (LinearLayoutManager) binding.rvBanner.getLayoutManager();
                int scrollPosition = myLayoutManager.findFirstVisibleItemPosition();
                dotAdapter.changeDot(scrollPosition);
            }
        });
        new PagerSnapHelper().attachToRecyclerView(binding.rvBanner);
        setupAutoSlider();
        setupUser();

        binding.ivBack.setOnClickListener(view -> finish());

        setting = sessionManager.getSetting();
        if (setting.isGooglePlaySwitch()) paymentGateways.add(getString(R.string.google_pay));
        if (setting.isStripeSwitch()) paymentGateways.add(getString(R.string.stripe));
        if (setting.isPaypalAndroidEnabled()) paymentGateways.add(STR_PAYPAL);
        if (setting.isRazorPayAndroidEnabled()) paymentGateways.add(STR_RAZORPAY);
        if (setting.isCashfreeAndroidEnabled()) paymentGateways.add(STR_CASHFREE);
        if (setting.isPaystackAndroidEnabled()) paymentGateways.add(STR_PAYSTACK);
        if (setting.isFlutterwaveAndroidEnabled()) paymentGateways.add(STR_FLUTTERWAVE);

        vipPlanAdapter.setOnPlanClickListener(plan -> selectedPlan = plan);
        binding.btnPurchase.setOnClickListener(view -> {
            if (selectedPlan == null) {
                Toast.makeText(this, R.string.first_select_the_plan, Toast.LENGTH_SHORT).show();
            } else {
                openBottomSheet(selectedPlan);
            }
        });

        binding.rvPlan.setAdapter(vipPlanAdapter);
        initData();
    }

    private void setupUser() {
        binding.tvName.setText(sessionManager.getUser().getName());
        binding.ivUser.setUserImage(sessionManager.getUser().getImage(), sessionManager.getUser().getAvatarFrameImage(), 25);
        PaymentConfiguration.init(this, sessionManager.getSetting().getStripePublishableKey());
        country = sessionManager.getSetting().getCurrency().getCountryCode();
        currency = sessionManager.getSetting().getCurrency().getCurrencyCode();
    }

    private void setupAutoSlider() {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int pos = 0;
            boolean forward = true;

            @Override
            public void run() {
                int count = vipImagesAdapter.getItemCount();
                if (count > 0) {
                    if (pos == count - 1) forward = false;
                    else if (pos == 0) forward = true;
                    pos = forward ? pos + 1 : pos - 1;
                    binding.rvBanner.smoothScrollToPosition(pos);
                    handler.postDelayed(this, 2500);
                }
            }
        };
        handler.postDelayed(runnable, 2500);
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Client ready");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "Billing Client disconnected");
            }
        });
    }

    private void initData() {
        List<VipPlanRoot.VipPlanItem> plans = sessionManager.getVipPlan();
        if (plans != null && !plans.isEmpty()) {
            selectedPlan = plans.get(0);
            vipPlanAdapter.submitList(plans);
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (!apiCalled) {
            apiCalled = true;
            callPurchaseApiGooglePay(purchase);
        }
        billingClient.consumeAsync(
                ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(),
                (billingResult, purchaseToken) -> Log.d(TAG, "Purchase consumed")
        );
    }

    private void callPurchaseApiGooglePay(Purchase purchase) {
        customDialogClass.show();

        JsonObject json = new JsonObject();
        json.addProperty("userId", sessionManager.getUser().getId());
        json.addProperty("planId", selectedPlan.getId());
        json.addProperty("productId", selectedPlan.getProductKey());
        json.addProperty("packageName", getPackageName());
        json.addProperty("token", purchase.getPurchaseToken());

        RetrofitBuilder.create().callPurchaseApiGooglePayVip(json).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                apiCalled = false;
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    sessionManager.saveUser(response.body().getUser());
                    customDialogClass.dismiss();
                    showSuccessPopup();
                } else {
                    customDialogClass.dismiss();
                    Toast.makeText(getApplicationContext(), getString(R.string.purchase_failed_please_try_again), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Purchase failed. Response code: " + response.code() + " Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                apiCalled = false;
                customDialogClass.dismiss();
                Log.e(TAG, "Error during purchase API call: ", t);
            }
        });
    }

    private void openBottomSheet(VipPlanRoot.VipPlanItem planItem) {
        bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        BottomSheetPaymentBinding sheetBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottom_sheet_payment, null, false);
        bottomSheetDialog.setContentView(sheetBinding.getRoot());
        bottomSheetDialog.show();

        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Show/hide payment options
        sheetBinding.lytgooglepay.setVisibility(paymentGateways.contains(STR_GP) ? View.VISIBLE : View.GONE);
        sheetBinding.lytStripe.setVisibility(paymentGateways.contains(STR_STRIPE) ? View.VISIBLE : View.GONE);
        sheetBinding.lytPaypal.setVisibility(
                paymentGateways.contains(STR_PAYPAL) ? View.VISIBLE : View.GONE);
        sheetBinding.lytRazorpay.setVisibility(
                paymentGateways.contains(STR_RAZORPAY) ? View.VISIBLE : View.GONE);
        sheetBinding.lytCashfree.setVisibility(
                paymentGateways.contains(STR_CASHFREE) ? View.VISIBLE : View.GONE);
        sheetBinding.lytPaystack.setVisibility(
                paymentGateways.contains(STR_PAYSTACK) ? View.VISIBLE : View.GONE);
        sheetBinding.lytFlutterwave.setVisibility(
                paymentGateways.contains(STR_FLUTTERWAVE) ? View.VISIBLE : View.GONE);


        final String[] selectedGateway = {null};

        Map<Integer, String> gatewayMap = new HashMap<>();
        gatewayMap.put(R.id.lytgooglepay, "google_pay");
        gatewayMap.put(R.id.lytStripe, "stripe");
        gatewayMap.put(R.id.lytPaypal, "paypal");
        gatewayMap.put(R.id.lytRazorpay, "razorpay");
        gatewayMap.put(R.id.lytCashfree, "cashfree");
        gatewayMap.put(R.id.lytPaystack, "paystack");
        gatewayMap.put(R.id.lytFlutterwave, "flutterwave");

        Map<Integer, ImageView> iconMap = new HashMap<>();
        iconMap.put(R.id.lytgooglepay, sheetBinding.ivSelectGoogle);
        iconMap.put(R.id.lytStripe, sheetBinding.ivSelectCredit);
        iconMap.put(R.id.lytPaypal, sheetBinding.ivSelectPaypal);
        iconMap.put(R.id.lytRazorpay, sheetBinding.ivSelectRazorPay);
        iconMap.put(R.id.lytCashfree, sheetBinding.ivSelectCashFree);
        iconMap.put(R.id.lytPaystack, sheetBinding.ivSelectPaystack);
        iconMap.put(R.id.lytFlutterwave, sheetBinding.ivSelectFlutterwave);

//        sheetBinding.btnPayNow.setBackgroundResource(R.drawable.tab_bg);
//        sheetBinding.btnPayNow.setTextColor(getColor(R.color.white_50));

        View.OnClickListener paymentOptionClickListener = view -> {


            sheetBinding.lytStripe.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytgooglepay.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytPaypal.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytRazorpay.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytCashfree.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytPaystack.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytFlutterwave.setBackgroundResource(R.drawable.bg_payment_unselected);

            // Highlight selected
            view.setBackgroundResource(R.drawable.bg_select_paymethod);
            sheetBinding.ivSelectCredit.setImageResource(R.drawable.radio_selected);

            selectedGateway[0] = gatewayMap.get(view.getId());

            for (ImageView iv : iconMap.values()) {
                iv.setImageResource(R.drawable.radio_unselected);
            }

            ImageView selectedIcon = iconMap.get(view.getId());
            if (selectedIcon != null) {
                selectedIcon.setImageResource(R.drawable.radio_selected);
            }


//            if (view == sheetBinding.lytgooglepay) {
//                selectedGateway[0] = STR_GP;
//                sheetBinding.lytgooglepay.setBackgroundResource(R.drawable.payment_option_selected_bg);
//                sheetBinding.lytStripe.setBackgroundResource(R.drawable.payment_option_bg);
//            } else if (view == sheetBinding.lytStripe) {
//                selectedGateway[0] = STR_STRIPE;
//                sheetBinding.lytStripe.setBackgroundResource(R.drawable.payment_option_selected_bg);
//                sheetBinding.lytgooglepay.setBackgroundResource(R.drawable.payment_option_bg);
//            }
//
//            sheetBinding.btnPayNow.setBackgroundResource(R.drawable.btn_bg);
//            sheetBinding.btnPayNow.setTextColor(getColor(R.color.white));
        };

        sheetBinding.lytgooglepay.setOnClickListener(paymentOptionClickListener);
        sheetBinding.lytStripe.setOnClickListener(paymentOptionClickListener);
        sheetBinding.lytPaypal.setOnClickListener(paymentOptionClickListener);
        sheetBinding.lytRazorpay.setOnClickListener(paymentOptionClickListener);
        sheetBinding.lytCashfree.setOnClickListener(paymentOptionClickListener);
        sheetBinding.lytPaystack.setOnClickListener(paymentOptionClickListener);
        sheetBinding.lytFlutterwave.setOnClickListener(paymentOptionClickListener);

        sheetBinding.btnPayNow.setOnClickListener(v -> {
            if (selectedGateway[0] == null) {
                Toast.makeText(VipPlanActivity.this, getString(R.string.please_select_a_payment_option), Toast.LENGTH_SHORT).show();
                return;
            }

            bottomSheetDialog.dismiss();

            paymentGateway = selectedGateway[0];

            if (STR_GP.equals(paymentGateway)) {
                if ("android.test.purchased".equalsIgnoreCase(planItem.getProductKey())) {
                    new PopupBuilder(this).showSimplePopup(getString(R.string.this_is_a_demo_version), getString(R.string.ok), () -> {
                    });
                } else {
                    makeGooglePurchase(planItem.getProductKey());
                }
            } else if (STR_STRIPE.equals(paymentGateway)) {
                initiateStripePurchase(planItem);
            }else if (STR_PAYPAL.equals(paymentGateway)) {
                launchPaypalPayment(planItem);
            } else if (STR_RAZORPAY.equals(paymentGateway)) {
                launchRazorpayPayment(planItem);
            } else if (STR_CASHFREE.equals(paymentGateway)) {
                launchCashfreePayment(planItem);
            } else if (STR_PAYSTACK.equals(paymentGateway)) {
                launchPaystackPayment(planItem);
            } else if (STR_FLUTTERWAVE.equals(paymentGateway)) {
                launchFlutterwavePayment(planItem);
            }

        });
    }


    private void makeGooglePurchase(String productId) {
        List<String> skuList = new ArrayList<>();
        skuList.add(productId);
        SkuDetailsParams params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP).build();
        billingClient.querySkuDetailsAsync(params, (billingResult, skuDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null && !skuDetailsList.isEmpty()) {
                SkuDetails skuDetails = skuDetailsList.get(0);
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build();
                billingClient.launchBillingFlow(this, billingFlowParams);
            }
        });
    }

    private void initiateStripePurchase(VipPlanRoot.VipPlanItem dataItem) {
        Log.d(TAG, "initiateStripePurchase: ");
        planId = dataItem.getId();
        setSelectedPlanId(dataItem.getId(), true);
        if (paymentGateway.equals(STR_GP)) {

            planId = dataItem.getId();
            productId = dataItem.getProductKey();
            planId = dataItem.getId();
            setSelectedPlanId(planId, true);
            makeGooglePurchase(productId);

        } else if (paymentGateway.equals(STR_STRIPE)) {
            customDialogClass.show();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("userId", sessionManager.getUser().getId());
            jsonObject.addProperty("planId", planId);
            jsonObject.addProperty("currency", currency);
            jsonObject.addProperty("isVip", true);

            Call<CreateUserStripe> call = RetrofitBuilder.create().getStripeCustomer(jsonObject);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<CreateUserStripe> call, @NonNull Response<CreateUserStripe> response) {
                    customerConfig = new PaymentSheet.CustomerConfiguration(response.body().getCustomer(), response.body().getEphemeralKey());
                    paymentClientSecret = response.body().getClientSecret();
                    paymentIntent = response.body().getPaymentIntent();
                    PaymentConfiguration.init(VipPlanActivity.this, response.body().getPublishableKey());

                    PaymentSheet.Address address = new PaymentSheet.Address.Builder()
                            .country("IN")
                            .build();
                    PaymentSheet.BillingDetails billingDetails = new PaymentSheet.BillingDetails.Builder()
                            .address(address)
                            .build();

                    PaymentSheet.BillingDetailsCollectionConfiguration billingDetailsCollectionConfiguration = new PaymentSheet.BillingDetailsCollectionConfiguration(
                            PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Always,
                            PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Never,
                            PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Never,
                            PaymentSheet.BillingDetailsCollectionConfiguration.AddressCollectionMode.Full,
                            true
                    );

                    final PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder(getString(R.string.app_name))
                            .customer(customerConfig)
                            .billingDetailsCollectionConfiguration(billingDetailsCollectionConfiguration)
                            .defaultBillingDetails(billingDetails)
                            .allowsPaymentMethodsRequiringShippingAddress(true)
                            .allowsDelayedPaymentMethods(true)
                            .build();

                    paymentSheet.presentWithPaymentIntent(
                            paymentClientSecret,
                            configuration
                    );

                    customDialogClass.dismiss();

                }

                @Override
                public void onFailure(@NonNull Call<CreateUserStripe> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: ", t);
                }
            });

        }
    }

    public void setSelectedPlanId(String selectedPlanId, boolean isVip) {
        this.selectedPlanId = selectedPlanId;
        this.isVip = isVip;
    }

    private void showSuccessPopup() {
        new PopupBuilder(this).showPopUpWithVector(R.drawable.vector_success, getString(R.string.you_are_vip_now), getString(R.string.restart_app), getString(R.string.continue_text), () -> {
            startActivity(new Intent(this, SplashActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finishAffinity();
        });
    }

    private void onPaymentSheetResult(final PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            Log.d("TAG", "Completed");
            callPurchaseDoneApi(planId, "Stripe");
            Toast.makeText(this, R.string.payment_done, Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, ((PaymentSheetResult.Failed) result).getError().getLocalizedMessage(), Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "Payment cancelled.");
        }
    }

    @Override
    protected void onDestroy() {
        if (billingClient != null && billingClient.isReady()) billingClient.endConnection();
        super.onDestroy();
    }

    public void callPurchaseDoneApi(String planId, String paymentGateway) {
        customDialogClass.show();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("planId", planId);
        jsonObject.addProperty("paymentGateway", paymentGateway);
        jsonObject.addProperty("payment_intent_id", paymentIntent);
        jsonObject.addProperty("currency", currency);

        Call<UserRoot> call = RetrofitBuilder.create().purchasePlanStripeVip(jsonObject);
        call.enqueue(new Callback<
                >() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {
                    if (response.body().getUser() != null && response.body().isStatus() && response.body().isStatus()) {
                        Toast.makeText(VipPlanActivity.this, R.string.purchased, Toast.LENGTH_SHORT).show();
                        sessionManager.saveUser(response.body().getUser());
                        customDialogClass.dismiss();
                        showSuccessPopup();
                    } else {
                        Log.d(TAG, "onResponse: 285");
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                Log.d(TAG, "onResponse: 293");
            }
        });
    }

    /**
     * PayPal
     **/
    private void initPaypal() {
        String cc = sessionManager.getSetting().getCurrency().getCurrencyCode();
        com.paypal.checkout.createorder.CurrencyCode paypalCurrency;
        try { paypalCurrency = com.paypal.checkout.createorder.CurrencyCode.valueOf(cc); }
        catch (Exception e) { paypalCurrency = com.paypal.checkout.createorder.CurrencyCode.USD; }

        com.paypal.checkout.config.CheckoutConfig config = new com.paypal.checkout.config.CheckoutConfig(
                getApplication(),
                PAYPAL_LIVE_CLIENT_ID,
                isPaypalLive() ? com.paypal.checkout.config.Environment.LIVE : com.paypal.checkout.config.Environment.SANDBOX,
                paypalCurrency,
                com.paypal.checkout.createorder.UserAction.PAY_NOW,
                com.paypal.checkout.config.PaymentButtonIntent.CAPTURE,
                "com.codder.ultimate://paypalpay" // ✅ SDK માટે same scheme જ રાખો
        );
        com.paypal.checkout.PayPalCheckout.setConfig(config);
    }

    private void launchPaypalPayment(VipPlanRoot.VipPlanItem plan) {
        planId = plan.getId();
        customDialogClass.show();

        String clientId = PAYPAL_LIVE_CLIENT_ID;
        String secret = PAYPAL_LIVE_SECRET;
        String baseUrl = isPaypalLive() ? "https://api-m.paypal.com" : "https://api-m.sandbox.paypal.com";
        String credentials = okhttp3.Credentials.basic(clientId, secret);

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                "grant_type=client_credentials",
                okhttp3.MediaType.parse("application/x-www-form-urlencoded"));

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl + "/v1/oauth2/token")
                .header("Authorization", credentials)
                .post(body).build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(VipPlanActivity.this, "PayPal init failed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                        paypalAccessToken = obj.getString("access_token");
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            launchPaypalWithToken(plan);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(VipPlanActivity.this, "PayPal parse error", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(VipPlanActivity.this, "PayPal auth failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void launchPaypalWithToken(VipPlanRoot.VipPlanItem plan) {
        planId = plan.getId();
        String baseUrl = isPaypalLive() ? "https://api-m.paypal.com" : "https://api-m.sandbox.paypal.com";
        String amountStr = String.format(java.util.Locale.US, "%.2f", (double) plan.getDollar());
        String cur = sessionManager.getSetting().getCurrency().getCurrencyCode();

        String orderJson = "{\"intent\":\"CAPTURE\",\"purchase_units\":[{\"amount\":{\"currency_code\":\""
                + cur + "\",\"value\":\"" + amountStr + "\"}}],\"application_context\":{"
                + "\"return_url\":\"com.codder.ultimate://vippaypal\","  // ✅ VipPlan deep link
                + "\"cancel_url\":\"com.codder.ultimate://vippaypal/cancel\","
                + "\"user_action\":\"PAY_NOW\",\"landing_page\":\"LOGIN\"}}";

        okhttp3.RequestBody body = okhttp3.RequestBody.create(orderJson, okhttp3.MediaType.parse("application/json"));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl + "/v2/checkout/orders")
                .header("Authorization", "Bearer " + paypalAccessToken)
                .header("Content-Type", "application/json")
                .post(body).build();

        customDialogClass.show();
        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(VipPlanActivity.this, "Order create failed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                        paypalOrderId = obj.getString("id");
                        String approveUrl = null;
                        org.json.JSONArray links = obj.getJSONArray("links");
                        for (int i = 0; i < links.length(); i++) {
                            org.json.JSONObject link = links.getJSONObject(i);
                            if ("approve".equals(link.getString("rel"))) {
                                approveUrl = link.getString("href");
                                break;
                            }
                        }
                        if (approveUrl != null) {
                            final String url = approveUrl;
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                openPayPalInBrowser(url);
                            });
                        } else {
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                Toast.makeText(VipPlanActivity.this, "Approve URL not found", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(VipPlanActivity.this, "Order parse error", Toast.LENGTH_SHORT).show();
                        });
                    }
                }else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    Log.e("PAYPAL", "Order failed: " + errorBody);

                    // ✅ Currency error detect
                    final String msg = errorBody.toLowerCase().contains("currency")
                            ? "Currency not supported"
                            : getString(R.string.something_went_wrong_text);

                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(VipPlanActivity.this, msg, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void openPayPalInBrowser(String approveUrl) {
        try {
            new androidx.browser.customtabs.CustomTabsIntent.Builder().setShowTitle(true).build()
                    .launchUrl(VipPlanActivity.this, android.net.Uri.parse(approveUrl));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(approveUrl)));
        }
    }

    private void capturePaypalOrder(String orderId) {
        customDialogClass.show();
        String baseUrl = isPaypalLive() ? "https://api-m.paypal.com" : "https://api-m.sandbox.paypal.com";
        okhttp3.RequestBody body = okhttp3.RequestBody.create("{}", okhttp3.MediaType.parse("application/json"));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl + "/v2/checkout/orders/" + orderId + "/capture")
                .header("Authorization", "Bearer " + paypalAccessToken)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .post(body).build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(VipPlanActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        paymentIntent = orderId;
                        callPurchaseDoneApi(planId, "PayPal");

                    });
                } else {
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(VipPlanActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    /**
     * RazorPay
     **/
    private void launchRazorpayPayment(VipPlanRoot.VipPlanItem plan) {
        planId = plan.getId();
//        String razorpayKey = IS_RAZORPAY_LIVE ? RAZORPAY_LIVE_KEY : RAZORPAY_TEST_KEY;
        com.razorpay.Checkout checkout = new com.razorpay.Checkout();
        checkout.setKeyID(RAZORPAY_LIVE_KEY);
        checkout.setImage(R.drawable.app_logo);
        try {
            String cur = sessionManager.getSetting().getCurrency().getCurrencyCode();
            java.util.List<String> zeroDecimal = java.util.Arrays.asList("JPY", "KRW", "VND", "IDR", "CLP", "PYG", "UGX", "XAF", "XOF");
            double planAmount = (double) plan.getDollar();
            int amount = zeroDecimal.contains(cur.toUpperCase())
                    ? (int) Math.round(planAmount)
                    : (int) Math.round(planAmount * 100);

            org.json.JSONObject options = new org.json.JSONObject();
            options.put("name", getString(R.string.app_name));
            options.put("description", "VIP Purchase");
            options.put("currency", cur);
            options.put("amount", amount);
            options.put("prefill.email", sessionManager.getUser().getEmail());
            options.put("prefill.contact", "");
            options.put("theme.color", "#360D46");
            checkout.open(VipPlanActivity.this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Payment init failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * RazorPay callbacks - VipPlanActivity implements PaymentResultWithDataListener
     **/

    @Override
    public void onPaymentSuccess(String razorpayPaymentId, com.razorpay.PaymentData paymentData) {
        paymentIntent = razorpayPaymentId;
        callPurchaseDoneApi(planId, "RazorPay");
    }

    @Override
    public void onPaymentError(int code, String description, com.razorpay.PaymentData paymentData) {
        if (code == com.razorpay.Checkout.PAYMENT_CANCELED) {
            Toast.makeText(this, getString(R.string.purchase_canceled), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Cashfree
     **/
    private void launchCashfreePayment(VipPlanRoot.VipPlanItem plan) {
        planId = plan.getId();
        customDialogClass.show();

        String appId = CASHFREE_LIVE_APP_ID ;
        String secret = CASHFREE_LIVE_SECRET ;
        String baseUrl = isCashfreeLive() ? "https://api.cashfree.com/pg/orders" : "https://sandbox.cashfree.com/pg/orders";
        String cur = sessionManager.getSetting().getCurrency().getCurrencyCode();
        String amountStr = String.format(java.util.Locale.US, "%.2f", (double) plan.getDollar());
        String orderId = "order_" + System.currentTimeMillis();

        String orderJson = "{\"order_id\":\"" + orderId + "\",\"order_amount\":" + amountStr
                + ",\"order_currency\":\"" + cur + "\",\"customer_details\":{"
                + "\"customer_id\":\"" + sessionManager.getUser().getId() + "\","
                + "\"customer_email\":\"" + sessionManager.getUser().getEmail() + "\","
                + "\"customer_phone\":\"9999999999\"},\"order_meta\":{"
                + "\"return_url\":\"https://yourapp.com/return?order_id={order_id}\"}}";

        okhttp3.RequestBody body = okhttp3.RequestBody.create(orderJson, okhttp3.MediaType.parse("application/json"));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl)
                .header("x-api-version", "2023-08-01")
                .header("x-client-id", appId)
                .header("x-client-secret", secret)
                .header("Content-Type", "application/json")
                .post(body).build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(VipPlanActivity.this, "Cashfree init failed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                        String sessionId = obj.getString("payment_session_id");
                        String createdOrderId = obj.getString("order_id");
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            openCashfreeCheckout(createdOrderId, sessionId);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(VipPlanActivity.this, "Cashfree parse error", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(VipPlanActivity.this, "Cashfree order failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void openCashfreeCheckout(String orderId, String paymentSessionId) {
        try {
            com.cashfree.pg.api.CFPaymentGatewayService.getInstance().setCheckoutCallback(cashfreeCallback);
            com.cashfree.pg.core.api.CFSession.Environment env = isCashfreeLive()
                    ? com.cashfree.pg.core.api.CFSession.Environment.PRODUCTION
                    : com.cashfree.pg.core.api.CFSession.Environment.SANDBOX;
            com.cashfree.pg.core.api.CFSession cfSession = new com.cashfree.pg.core.api.CFSession.CFSessionBuilder()
                    .setEnvironment(env).setPaymentSessionID(paymentSessionId).setOrderId(orderId).build();
            com.cashfree.pg.core.api.CFTheme cfTheme = new com.cashfree.pg.core.api.CFTheme.CFThemeBuilder()
                    .setNavigationBarBackgroundColor("#360D46").setNavigationBarTextColor("#FFFFFF")
                    .setButtonBackgroundColor("#360D46").setButtonTextColor("#FFFFFF")
                    .setPrimaryTextColor("#000000").setSecondaryTextColor("#333333").build();
            com.cashfree.pg.ui.api.CFDropCheckoutPayment cfDrop = new com.cashfree.pg.ui.api.CFDropCheckoutPayment.CFDropCheckoutPaymentBuilder()
                    .setSession(cfSession).setCFNativeCheckoutUITheme(cfTheme).build();
            com.cashfree.pg.api.CFPaymentGatewayService.getInstance().doPayment(VipPlanActivity.this, cfDrop);
        } catch (com.cashfree.pg.core.api.exception.CFException e) {
            Toast.makeText(this, "Cashfree checkout failed", Toast.LENGTH_SHORT).show();
        }
    }

    private final com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback cashfreeCallback =
            new com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback() {
                @Override
                public void onPaymentVerify(String orderId) {

                    runOnUiThread(() -> {
                        paymentIntent = orderId;
                        callPurchaseDoneApi(planId, "Cashfree");
                    });

                }

                @Override
                public void onPaymentFailure(com.cashfree.pg.core.api.utils.CFErrorResponse err, String orderId) {
                    runOnUiThread(() -> Toast.makeText(VipPlanActivity.this,
                            "USER_DROPPED".equals(err.getStatus())
                                    ? getString(R.string.purchase_canceled)
                                    : getString(R.string.purchase_failed),
                            Toast.LENGTH_SHORT).show());
                }
            };

    /**
     * Paystack
     **/
    private void launchPaystackPayment(VipPlanRoot.VipPlanItem plan) {
        planId = plan.getId();
        customDialogClass.show();

        String secretKey = PAYSTACK_LIVE_SECRET_KEY;
        String paystackCurrency = isPaystackLive()
                ? sessionManager.getSetting().getCurrency().getCurrencyCode() : "NGN";
        int amountInSubunit = (int) Math.round((double) plan.getDollar() * 100);

        String initJson = "{\"email\":\"" + sessionManager.getUser().getEmail() + "\","
                + "\"amount\":" + amountInSubunit + ",\"currency\":\"" + paystackCurrency + "\","
                + "\"reference\":\"pay_" + System.currentTimeMillis() + "\","
                + "\"callback_url\":\"https://paystack.com/close\"}";

        okhttp3.RequestBody body = okhttp3.RequestBody.create(initJson, okhttp3.MediaType.parse("application/json"));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.paystack.co/transaction/initialize")
                .header("Authorization", "Bearer " + secretKey)
                .header("Content-Type", "application/json")
                .post(body).build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(VipPlanActivity.this, "Paystack init failed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                        org.json.JSONObject data = obj.getJSONObject("data");
                        String accessCode = data.getString("access_code");
                        String reference = data.getString("reference");
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            openPaystackCheckout(accessCode, reference);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(VipPlanActivity.this, "Paystack parse error", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(VipPlanActivity.this, "Paystack init failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void openPaystackCheckout(String accessCode, String reference) {
        String checkoutUrl = "https://checkout.paystack.com/" + accessCode;
        Intent intent = new Intent(VipPlanActivity.this, com.codder.ultimate.activity.PaystackWebViewActivity.class);
        intent.putExtra(com.codder.ultimate.activity.PaystackWebViewActivity.EXTRA_URL, checkoutUrl);
        intent.putExtra(com.codder.ultimate.activity.PaystackWebViewActivity.EXTRA_REFERENCE, reference);
        paystackLauncher.launch(intent);
    }

    private void verifyPaystackPayment(String reference) {
        customDialogClass.show();
        String secretKey =PAYSTACK_LIVE_SECRET_KEY;
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.paystack.co/transaction/verify/" + reference)
                .header("Authorization", "Bearer " + secretKey).get().build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(VipPlanActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                        String status = obj.getJSONObject("data").getString("status");
                        if ("success".equals(status)) {
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                paymentIntent = reference;
                                callPurchaseDoneApi(planId, "Paystack");
                            });
                        } else {
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                Toast.makeText(VipPlanActivity.this, getString(R.string.purchase_canceled), Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(VipPlanActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        });
    }

    private void launchFlutterwavePayment(VipPlanRoot.VipPlanItem plan) {
        planId = plan.getId();
        customDialogClass.show();

        String cur = sessionManager.getSetting().getCurrency().getCurrencyCode();
        String email = sessionManager.getUser().getEmail();
        String name = sessionManager.getUser().getName();
        String txRef = "flw_" + System.currentTimeMillis();
        String amountStr = String.format(java.util.Locale.US, "%.2f", (double) plan.getDollar());

        String paymentJson = "{"
                + "\"tx_ref\":\"" + txRef + "\","
                + "\"amount\":\"" + amountStr + "\","
                + "\"currency\":\"" + cur + "\","
                + "\"redirect_url\":\"https://flutterwave.com/pay/close\","
                + "\"customer\":{"
                + "\"email\":\"" + email + "\","
                + "\"name\":\"" + name + "\""
                + "},"
                + "\"customizations\":{"
                + "\"title\":\"" + getString(R.string.app_name) + "\","
                + "\"description\":\"VIP Purchase\","
                + "\"logo\":\"\""
                + "}"
                + "}";

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                paymentJson, okhttp3.MediaType.parse("application/json"));

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.flutterwave.com/v3/payments")
                .header("Authorization", "Bearer " + FLUTTERWAVE_SECRET_KEY)
                .header("Content-Type", "application/json")
                .post(body).build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(VipPlanActivity.this,
                            getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                        if ("success".equals(obj.getString("status"))) {
                            String paymentLink = obj.getJSONObject("data").getString("link");
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                openFlutterwaveCheckout(paymentLink, txRef);
                            });
                        } else {
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                Toast.makeText(VipPlanActivity.this,
                                        getString(R.string.something_went_wrong_text),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(VipPlanActivity.this,
                                    getString(R.string.something_went_wrong_text),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    Log.e("FLW", "Failed: " + errorBody);
                    final String msg = errorBody.toLowerCase().contains("currency")
                            ? getString(R.string.something_went_wrong_text)
                            : getString(R.string.something_went_wrong_text);
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(VipPlanActivity.this, msg, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void openFlutterwaveCheckout(String paymentLink, String txRef) {
        Intent intent = new Intent(VipPlanActivity.this, FlutterwaveWebViewActivity.class);
        intent.putExtra(FlutterwaveWebViewActivity.EXTRA_URL, paymentLink);
        intent.putExtra(FlutterwaveWebViewActivity.EXTRA_REFERENCE, txRef);
        flutterwaveLauncher.launch(intent);
    }

    private void verifyFlutterwavePayment(String txRef) {
        customDialogClass.show();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.flutterwave.com/v3/transactions/verify_by_reference?tx_ref=" + txRef)
                .header("Authorization", "Bearer " + FLUTTERWAVE_SECRET_KEY)
                .get().build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(VipPlanActivity.this,
                            getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                        String status = obj.getJSONObject("data").getString("status");
                        if ("successful".equals(status)) {
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                paymentIntent = txRef;
                                callPurchaseDoneApi(planId, "Flutterwave");
                            });
                        } else {
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                Toast.makeText(VipPlanActivity.this,
                                        getString(R.string.purchase_canceled),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(VipPlanActivity.this,
                                    getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(VipPlanActivity.this,
                                getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private boolean isPaypalLive() {
        return !PAYPAL_LIVE_CLIENT_ID.contains("sandbox")
                && PAYPAL_LIVE_CLIENT_ID.length() < 60;
    }

    private boolean isCashfreeLive() {
        return !CASHFREE_LIVE_SECRET.startsWith("TEST");
    }

    private String getCashfreeBaseUrl() {
        return isCashfreeLive()
                ? "https://api.cashfree.com/pg/orders"
                : "https://sandbox.cashfree.com/pg/orders";
    }

    private boolean isPaystackLive() {
        return PAYSTACK_LIVE_SECRET_KEY.startsWith("sk_live_");
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPaypal(); // ✅ VipPlan scheme ensure
    }

}