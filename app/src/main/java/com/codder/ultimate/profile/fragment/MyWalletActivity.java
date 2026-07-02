package com.codder.ultimate.profile.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.activity.FlutterwaveWebViewActivity;
import com.codder.ultimate.activity.PaystackWebViewActivity;
import com.codder.ultimate.databinding.ActivityMyWalletBinding;
import com.codder.ultimate.databinding.BottomSheetPaymentBinding;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.profile.activity.CoinSellerListActivity;
import com.codder.ultimate.profile.activity.RecordActivity;
import com.codder.ultimate.profile.adapter.CoinPurchaseAdapter;
import com.codder.ultimate.profile.modelclass.CreateUserStripe;
import com.codder.ultimate.profile.modelclass.DiamondPlanRoot;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonObject;
import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.config.CheckoutConfig;
import com.paypal.checkout.config.Environment;
import com.paypal.checkout.config.PaymentButtonIntent;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.UserAction;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment to handle in-app purchases via Google Play Billing and Stripe.
 */
public class MyWalletActivity extends BaseActivity implements PurchasesUpdatedListener , com.razorpay.PaymentResultWithDataListener,com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback{

    private static final String TAG = "MyWalletActivity";

    private ActivityMyWalletBinding binding;
    private SessionManager sessionManager;
    private BillingClient billingClient;
    private AtomicBoolean isPurchaseInProgress = new AtomicBoolean(false);
    private PaymentSheet paymentSheet;
    private String paymentClientSecret;
    private String currentPlanId;
    private DiamondPlanRoot.DiamondPlanItem selectedPlan;
    private final List<String> paymentGateways = new ArrayList<>();

    private String PAYPAL_LIVE_CLIENT_ID = "";
    private String PAYPAL_LIVE_SECRET = "";

    private String paypalAccessToken = "";
    private String paypalOrderId = "";
    private OkHttpClient okHttpClient = new OkHttpClient();

    private String RAZORPAY_LIVE_KEY = "";

    private  String CASHFREE_LIVE_APP_ID = "";
    private  String CASHFREE_LIVE_SECRET = "";

    private  String PAYSTACK_LIVE_SECRET_KEY = "";

    private ActivityResultLauncher<Intent> paystackLauncher;

    // Flutterwave
    private String FLUTTERWAVE_SECRET_KEY = "";
    private ActivityResultLauncher<Intent> flutterwaveLauncher;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Uri data = intent.getData();
        Log.d("PAYPAL", "onNewIntent: " + data);

        if (data != null && data.toString().contains("paypalpay")) {
            // cancel check
            if (data.toString().contains("cancel")) {
                Toast.makeText(this, getString(R.string.purchase_canceled), Toast.LENGTH_SHORT).show();
                return;
            }

            String token = data.getQueryParameter("token"); // ✅ token = orderId
            Log.d("PAYPAL", "PayPal token: " + token);

            if (token != null && !token.isEmpty()) {

                paypalOrderId = token;
                capturePaypalOrder(token);
            } else if (paypalOrderId != null && !paypalOrderId.isEmpty()) {
                capturePaypalOrder(paypalOrderId);
            }
        }

        // ✅ Paystack return handle
        if (data != null && data.toString().contains("paystack")) {
            String status = data.getQueryParameter("status");
            String reference = data.getQueryParameter("reference");
            Log.d("PAYSTACK", "Return: status=" + status + " ref=" + reference);

            if ("success".equals(status) && reference != null) {
                callPurchaseApi(reference, "Paystack");
            } else {
                Toast.makeText(this, getString(R.string.purchase_canceled),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_wallet);
        sessionManager = new SessionManager(MyWalletActivity.this);
        PAYPAL_LIVE_CLIENT_ID = sessionManager.getSetting().getPaypalClientId();
        PAYPAL_LIVE_SECRET = sessionManager.getSetting().getPaypalSecretKey();
        RAZORPAY_LIVE_KEY = sessionManager.getSetting().getRazorSecretKey();
        CASHFREE_LIVE_APP_ID = sessionManager.getSetting().getCashfreeClientId();
        CASHFREE_LIVE_SECRET = sessionManager.getSetting().getCashfreeClientSecret();
        PAYSTACK_LIVE_SECRET_KEY = sessionManager.getSetting().getPaystackSecretKey();
        FLUTTERWAVE_SECRET_KEY = sessionManager.getSetting().getFlutterwaveSecretKey();


        flutterwaveLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == FlutterwaveWebViewActivity.RESULT_SUCCESS
                            && result.getData() != null) {
                        String ref = result.getData()
                                .getStringExtra(FlutterwaveWebViewActivity.EXTRA_REFERENCE);
                        Log.d("FLW", "✅ Success: " + ref);
                        verifyFlutterwavePayment(ref);
                    } else {
                        Toast.makeText(this, getString(R.string.purchase_canceled),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        paystackLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == PaystackWebViewActivity.RESULT_SUCCESS
                            && result.getData() != null) {
                        String ref = result.getData()
                                .getStringExtra(PaystackWebViewActivity.EXTRA_REFERENCE);
                        Log.d("PAYSTACK", "✅ Auto redirect success: " + ref);
                        verifyPaystackPayment(ref); // ✅ verify then backend notify
                    } else {
                        Toast.makeText(this, getString(R.string.purchase_canceled),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        initStripe();
        setupBillingClient();
        initUi();
        fetchDiamondPlans();
    }

    /** Initialize Stripe PaymentSheet **/
    private void initStripe() {
        String stripeKey = sessionManager.getSetting().getStripePublishableKey();
        if (stripeKey != null && !stripeKey.isEmpty()) {
            if (sessionManager.getSetting().getStripePublishableKey() != null
                    && !sessionManager.getSetting().getStripePublishableKey().isEmpty()) {
                PaymentConfiguration.init(MyWalletActivity.this, sessionManager.getSetting().getStripePublishableKey());
            }

        }
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
    }

    /** Populate UI elements and available gateways **/
    private void initUi() {
        binding.tvMyCoins.setText(RayziUtils.formatCoin(sessionManager.getUser().getDiamond()));
        if (sessionManager.getSetting().isGooglePlaySwitch()) {
            paymentGateways.add("google_pay");
        }
        if (sessionManager.getSetting().isStripeSwitch()) {
            paymentGateways.add("stripe");
        }
        if (sessionManager.getSetting().isPaypalAndroidEnabled()) {
            paymentGateways.add("paypal");
        }

        if (sessionManager.getSetting().isRazorPayAndroidEnabled()) {
            paymentGateways.add("razorpay");
        }
        if (sessionManager.getSetting().isCashfreeAndroidEnabled()) {
            paymentGateways.add("cashfree");
        }
        if (sessionManager.getSetting().isPaystackAndroidEnabled()) {
            paymentGateways.add("paystack");
        }
        if (sessionManager.getSetting().isFlutterwaveAndroidEnabled()) {
            paymentGateways.add("flutterwave");
        }

        binding.lytCoinSeller.setOnClickListener(v -> startActivity(new Intent(MyWalletActivity.this, CoinSellerListActivity.class)));

        binding.layoutMyIncome.setOnClickListener(v -> startActivity(new Intent(MyWalletActivity.this, MyIncomeActivity.class)));

        binding.tvHistory.setOnClickListener(v -> startActivity(new Intent(MyWalletActivity.this, RecordActivity.class)));
    }

    /** Build and connect BillingClient **/
    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(MyWalletActivity.this)
                .enablePendingPurchases()
                .setListener(this)
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "BillingClient setup finished");
                } else {
                    Log.e(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.w(TAG, "Service disconnected, retrying…");
                billingClient.startConnection(this);
            }
        });
    }

    /** Load available diamond plans from server **/
    private void fetchDiamondPlans() {
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.layoutMain.setVisibility(View.GONE);

        RetrofitBuilder.create()
                .getDiamondsPlan()
                .enqueue(new Callback<DiamondPlanRoot>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<DiamondPlanRoot> call,
                            @NonNull Response<DiamondPlanRoot> response
                    ) {
                        binding.shimmer.setVisibility(View.GONE);
                        binding.layoutMain.setVisibility(View.VISIBLE);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isStatus()
                        ) {
                            populatePlans(response.body().getCoinPlan());
                        } else {
                            Log.e(TAG, "Failed to load plans: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<DiamondPlanRoot> call,
                            @NonNull Throwable t
                    ) {
                        binding.shimmer.setVisibility(View.GONE);
                        binding.layoutMain.setVisibility(View.VISIBLE);
                        Log.e(TAG, "Plan fetch error", t);
                        Toast.makeText(
                                MyWalletActivity.this,
                                getString(R.string.error_loading_plans),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    /** Display plans in UI **/
    private void populatePlans(List<DiamondPlanRoot.DiamondPlanItem> plans) {
        List<DiamondPlanRoot.DiamondPlanItem> normalPlans = new ArrayList<>();
        DiamondPlanRoot.DiamondPlanItem popularPlan = null;

        for (DiamondPlanRoot.DiamondPlanItem plan : plans) {
            if (plan.isTop()) {
                popularPlan = plan;
            } else {
                normalPlans.add(plan);
            }
        }
        binding.layPopularPurchase.setVisibility(View.GONE);

        if (popularPlan != null) {

            binding.layPopularPurchase.setVisibility(View.VISIBLE);
            binding.tvPopulatPlanCoin.setText("X " + popularPlan.getDiamonds());
            binding.tvPopularPlanAmount.setText(
                    (sessionManager.getSetting().getCurrency().getSymbol()) + " "
                            + (popularPlan.getDollar())
            );
            DiamondPlanRoot.DiamondPlanItem finalPopularPlan = popularPlan;
            binding.layPopularPurchase.setOnClickListener(v -> {
                        showPaymentOptions(finalPopularPlan);
                    }
            );
        }

        CoinPurchaseAdapter adapter = new CoinPurchaseAdapter(this::showPaymentOptions);
        adapter.submitList(normalPlans);
        binding.rvRecharge.setAdapter(adapter);

    }

    /** Show bottom sheet with Google-Pay / Stripe choices **/
    private void showPaymentOptions(DiamondPlanRoot.DiamondPlanItem plan) {
        selectedPlan = plan;
        BottomSheetDialog sheet = new BottomSheetDialog(MyWalletActivity.this, R.style.CustomBottomSheetDialogTheme);
        BottomSheetPaymentBinding sheetBinding = DataBindingUtil.inflate(
                LayoutInflater.from(MyWalletActivity.this),
                R.layout.bottom_sheet_payment,
                null,
                false
        );
        sheet.setContentView(sheetBinding.getRoot());
        BottomSheetBehavior.from(
                        sheet.findViewById(com.google.android.material.R.id.design_bottom_sheet))
                .setState(BottomSheetBehavior.STATE_EXPANDED);

        sheetBinding.btnClose.setOnClickListener(v -> sheet.dismiss());
        sheetBinding.lytgooglepay.setVisibility(
                paymentGateways.contains("google_pay") ? View.VISIBLE : View.GONE
        );
        sheetBinding.lytStripe.setVisibility(
                paymentGateways.contains("stripe") ? View.VISIBLE : View.GONE
        );
        sheetBinding.lytPaypal.setVisibility(
                paymentGateways.contains("paypal") ? View.VISIBLE : View.GONE
        );

        sheetBinding.lytRazorpay.setVisibility(
                paymentGateways.contains("razorpay") ? View.VISIBLE : View.GONE
        );

        sheetBinding.lytCashfree.setVisibility(
                paymentGateways.contains("cashfree") ? View.VISIBLE : View.GONE
        );

        sheetBinding.lytPaystack.setVisibility(
                paymentGateways.contains("paystack") ? View.VISIBLE : View.GONE
        );
        sheetBinding.lytFlutterwave.setVisibility(
                paymentGateways.contains("flutterwave") ? View.VISIBLE : View.GONE);

        final String[] selectedGateway = {null}; // tracking selectionV


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


        View.OnClickListener selectionListener = v -> {
            // Reset all to default background
            sheetBinding.lytStripe.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytgooglepay.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytPaypal.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytRazorpay.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytCashfree.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytPaystack.setBackgroundResource(R.drawable.bg_payment_unselected);
            sheetBinding.lytFlutterwave.setBackgroundResource(R.drawable.bg_payment_unselected);

            // Highlight selected
            v.setBackgroundResource(R.drawable.bg_select_paymethod);
            sheetBinding.ivSelectCredit.setImageResource(R.drawable.radio_selected);

            selectedGateway[0] = gatewayMap.get(v.getId());

            for (ImageView iv : iconMap.values()) {
                iv.setImageResource(R.drawable.radio_unselected);
            }

            ImageView selectedIcon = iconMap.get(v.getId());
            if (selectedIcon != null) {
                selectedIcon.setImageResource(R.drawable.radio_selected);
            }

        };

        // Set selection handlers
        sheetBinding.lytStripe.setOnClickListener(selectionListener);
        sheetBinding.lytgooglepay.setOnClickListener(selectionListener);
        sheetBinding.lytPaypal.setOnClickListener(selectionListener);
        sheetBinding.lytRazorpay.setOnClickListener(selectionListener);
        sheetBinding.lytCashfree.setOnClickListener(selectionListener);
        sheetBinding.lytPaystack.setOnClickListener(selectionListener);
        sheetBinding.lytFlutterwave.setOnClickListener(selectionListener);

        // Handle Pay Now
        sheetBinding.btnPayNow.setOnClickListener(v -> {
            if (selectedGateway[0] == null) {
                Toast.makeText(MyWalletActivity.this, getString(R.string.please_select_a_payment_option), Toast.LENGTH_SHORT).show();
                return;
            }

            sheet.dismiss();

            switch (selectedGateway[0]) {
                case "google_pay":
                    if (plan.getProductKey().equalsIgnoreCase("android.test.purchased")) {
                        new PopupBuilder(MyWalletActivity.this).showSimplePopup(
                                getString(R.string.this_is_a_demo_version),
                                R.drawable.ic_demoversion,
                                getString(R.string.ok),
                                () -> {
                                }
                        );
                    } else {
                        launchGooglePurchase(plan);
                    }
                    break;

                case "stripe":
                    launchStripePurchase(plan);
                    break;
                case "paypal":
                    launchPaypalPayment(plan);
                    break;
                case "razorpay":
                    launchRazorpayPayment(plan);
                    break;
                case "cashfree":
                    launchCashfreePayment(plan);
                    break;
                case "paystack":
                    launchPaystackPayment(plan);
                    break;
                case "flutterwave":
                    launchFlutterwavePayment(plan);
                    break;
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            sheet.show();
        }, 200);
    }



    /** Start a Google-Play purchase flow **/
    private void launchGooglePurchase(DiamondPlanRoot.DiamondPlanItem plan) {
        if (!billingClient.isReady()) {
            Toast.makeText(this, getString(R.string.billing_not_ready), Toast.LENGTH_SHORT).show();
            setupBillingClient();
            return;
        }

        String productId = plan.getProductKey() == null ? "" : plan.getProductKey().trim();
        if (productId.isEmpty()) {
            Log.e(TAG, "Empty productId for plan: " + plan.getId());
            Toast.makeText(this, R.string.purchase_error, Toast.LENGTH_SHORT).show();
            return;
        }
        currentPlanId = plan.getId();

        QueryProductDetailsParams.Product product =
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build();

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(List.of(product))
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            runOnUiThread(() -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && productDetailsList != null && !productDetailsList.isEmpty()) {
                    ProductDetails details = productDetailsList.get(0);
                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(List.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(details)
                                            .build()
                            ))
                            .build();
                    billingClient.launchBillingFlow(MyWalletActivity.this, flowParams);
                } else {
                    Log.e(TAG, "Error querying product details: " + billingResult.getDebugMessage());
                    Toast.makeText(this, R.string.purchase_error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    /** Start a Stripe-based purchase flow **/
    private void launchStripePurchase(DiamondPlanRoot.DiamondPlanItem plan) {
        customDialogClass.show();
        currentPlanId = plan.getId();
        JsonObject payload = new JsonObject();
        payload.addProperty("userId", sessionManager.getUser().getId());
        payload.addProperty("planId", plan.getId());
        payload.addProperty("currency", sessionManager.getSetting().getCurrency().getCurrencyCode());
        payload.addProperty("isVip", false);

        RetrofitBuilder.create()
                .getStripeCustomer(payload)
                .enqueue(new Callback<CreateUserStripe>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<CreateUserStripe> call,
                            @NonNull Response<CreateUserStripe> response
                    ) {
                        customDialogClass.dismiss();

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isStatus()
                        ) {
                            CreateUserStripe data = response.body();
                            paymentClientSecret = data.getClientSecret();
                            PaymentConfiguration.init(MyWalletActivity.this, data.getPublishableKey());
                            PaymentSheet.CustomerConfiguration customerConfig = new PaymentSheet.CustomerConfiguration(data.getCustomer(), data.getEphemeralKey());
                            paymentClientSecret = data.getClientSecret();

                            PaymentSheet.Address address = new PaymentSheet.Address.Builder()
                                    .country("IN")
                                    .build();

                            PaymentSheet.BillingDetails billingDetails = new PaymentSheet.BillingDetails.Builder()
                                    .address(address)
                                    .build();

                            PaymentSheet.BillingDetailsCollectionConfiguration billingDetailsCollectionConfiguration =
                                    new PaymentSheet.BillingDetailsCollectionConfiguration(
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

                            paymentSheet.presentWithPaymentIntent(paymentClientSecret, configuration);

                        } else {
                            Toast.makeText(MyWalletActivity.this, getString(R.string.payment_init_failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CreateUserStripe> call, @NonNull Throwable t) {
                        Log.e(TAG, "Stripe init error", t);
                        customDialogClass.dismiss();
                        Toast.makeText(MyWalletActivity.this, getString(R.string.payment_init_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Handle the result of Google-Play purchases **/
    @Override
    public void onPurchasesUpdated(
            @NonNull BillingResult billingResult,
            @Nullable List<Purchase> purchases) {
        switch (billingResult.getResponseCode()) {
            case BillingClient.BillingResponseCode.OK:
                if (purchases != null && !purchases.isEmpty()
                        && isPurchaseInProgress.compareAndSet(false, true)
                ) {
                    handleGooglePurchase(purchases.get(0));
                }
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:
                Toast.makeText(MyWalletActivity.this, getString(R.string.purchase_canceled), Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.e(TAG, "Purchase failed: " + billingResult.getDebugMessage());
                Toast.makeText(MyWalletActivity.this, getString(R.string.purchase_error), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /** Consume and acknowledge Google-Play purchase **/
    private void handleGooglePurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.consumeAsync(
                consumeParams,
                (billingResult, purchaseToken) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        runOnUiThread(() -> callGooglePurchaseApi(purchaseToken));
                    } else {
                        Log.e(TAG, "Consume failed: " + billingResult.getDebugMessage());
                        isPurchaseInProgress.set(false);
                    }
                }
        );
    }

    /** Notify backend of a successful Google-Play purchase **/
    private void callGooglePurchaseApi(String token) {
        customDialogClass.show();
        JsonObject payload = new JsonObject();
        payload.addProperty("userId", sessionManager.getUser().getId());
        payload.addProperty("planId", currentPlanId);
        payload.addProperty("token", token);

        RetrofitBuilder.create()
                .callPurchaseApiGooglePayDiamond(payload)
                .enqueue(new Callback<UserRoot>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<UserRoot> call,
                            @NonNull Response<UserRoot> response
                    ) {
                        customDialogClass.dismiss();
                        isPurchaseInProgress.set(false);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isStatus()
                        ) {
                            sessionManager.saveUser(response.body().getUser());
                            binding.tvMyCoins.setText(
                                    RayziUtils.formatCoin(response.body().getUser().getDiamond())
                            );
                            Toast.makeText(MyWalletActivity.this, getString(R.string.purchase_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyWalletActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserRoot> call, @NonNull Throwable t) {
                        isPurchaseInProgress.set(false);
                        customDialogClass.dismiss();
                        Log.e(TAG, "Purchase API error", t);
                        Toast.makeText(MyWalletActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Handle the result of Stripe PaymentSheet **/
    private void onPaymentSheetResult(@NonNull PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            callStripePurchaseApi(); // or callPurchaseDoneApi(...)
            Toast.makeText(MyWalletActivity.this, getString(R.string.payment_done), Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Canceled) {
            Log.d("TAG", "Payment canceled");
        } else if (result instanceof PaymentSheetResult.Failed) {
            Log.e("TAG", "Payment failed: ", ((PaymentSheetResult.Failed) result).getError());
            Toast.makeText(MyWalletActivity.this, ((PaymentSheetResult.Failed) result).getError().toString(), Toast.LENGTH_SHORT).show();
        }
    }


    /** Notify backend of a successful Stripe payment **/
    private void callStripePurchaseApi() {
        String paymentGateway = "Stripe";
        customDialogClass.show();
        JsonObject payload = new JsonObject();
        payload.addProperty("userId", sessionManager.getUser().getId());
        payload.addProperty("planId", currentPlanId);
        payload.addProperty("currency", paymentGateway);
        payload.addProperty("payment_intent_id", paymentClientSecret);

        RetrofitBuilder.create()
                .purchasePlanStripeDiamonds(payload)
                .enqueue(new Callback<UserRoot>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<UserRoot> call,
                            @NonNull Response<UserRoot> response
                    ) {
                        customDialogClass.dismiss();
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isStatus()
                        ) {
                            sessionManager.saveUser(response.body().getUser());
                            binding.tvMyCoins.setText(
                                    RayziUtils.formatCoin(response.body().getUser().getDiamond())
                            );
                            Toast.makeText(MyWalletActivity.this, getString(R.string.purchase_success), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserRoot> call, @NonNull Throwable t) {
                        Log.e(TAG, "Stripe API error", t);
                        customDialogClass.dismiss();
                        Toast.makeText(MyWalletActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** PayPal payment gateway **/
    private void initPaypal() {

        String currencyCode = sessionManager.getSetting().getCurrency().getCurrencyCode();
        CurrencyCode paypalCurrency;
        try {
            paypalCurrency = CurrencyCode.valueOf(currencyCode);
        } catch (Exception e) {
            paypalCurrency = CurrencyCode.USD; // fallback
        }

        CheckoutConfig config = new CheckoutConfig(
                getApplication(),
                PAYPAL_LIVE_CLIENT_ID,
                isPaypalLive() ? Environment.LIVE : Environment.SANDBOX,
                paypalCurrency,
                UserAction.PAY_NOW,
                PaymentButtonIntent.CAPTURE,
                "com.volaparty.voice://paypalpay"
        );
        PayPalCheckout.setConfig(config);
    }

    private void fetchPaypalAccessToken(DiamondPlanRoot.DiamondPlanItem plan) {
        customDialogClass.show();


        String clientId = PAYPAL_LIVE_CLIENT_ID;
        String secret = PAYPAL_LIVE_SECRET;
        String baseUrl = isPaypalLive()
                ? "https://api-m.paypal.com"
                : "https://api-m.sandbox.paypal.com";

        String credentials = okhttp3.Credentials.basic(clientId, secret);

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                "grant_type=client_credentials",
                okhttp3.MediaType.parse("application/x-www-form-urlencoded")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl + "/v1/oauth2/token")
                .header("Authorization", credentials)
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(MyWalletActivity.this,
                            "PayPal init failed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(json);
                        paypalAccessToken = obj.getString("access_token");
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            launchPaypalWithToken(plan);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(MyWalletActivity.this,
                                    "PayPal token parse error", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(MyWalletActivity.this,
                                "PayPal auth failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void launchPaypalWithToken(DiamondPlanRoot.DiamondPlanItem plan) {
        currentPlanId = plan.getId();


        String baseUrl = isPaypalLive()
                ? "https://api-m.paypal.com"
                : "https://api-m.sandbox.paypal.com";

        String amountStr = String.format(java.util.Locale.US, "%.2f", (double) plan.getDollar());
        String currency = sessionManager.getSetting().getCurrency().getCurrencyCode();


        String orderJson = "{"
                + "\"intent\":\"CAPTURE\","
                + "\"purchase_units\":[{"
                + "\"amount\":{"
                + "\"currency_code\":\"" + currency + "\","
                + "\"value\":\"" + amountStr + "\""
                + "}"
                + "}],"
                + "\"application_context\":{"
                + "\"return_url\":\"com.volaparty.voice://paypalpay\","
                + "\"cancel_url\":\"com.volaparty.voice://paypalpay/cancel\","
                + "\"user_action\":\"PAY_NOW\","
                + "\"landing_page\":\"LOGIN\""
                + "}"
                + "}";

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                orderJson,
                okhttp3.MediaType.parse("application/json")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl + "/v2/checkout/orders")
                .header("Authorization", "Bearer " + paypalAccessToken)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        customDialogClass.show();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(MyWalletActivity.this,
                            "Order create failed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Log.d("PAYPAL", "Order response: " + json);
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(json);
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

                        Log.d("PAYPAL", "Order ID: " + paypalOrderId);
                        Log.d("PAYPAL", "Approve URL: " + approveUrl);

                        if (approveUrl != null) {
                            final String finalApproveUrl = approveUrl;
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();

                                openPayPalInBrowser(finalApproveUrl);
                            });
                        } else {
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                Toast.makeText(MyWalletActivity.this,
                                        "Approve URL not found", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(MyWalletActivity.this,
                                    "Order parse error", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    Log.e("PAYPAL", "Order create failed: " + errorBody);
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(MyWalletActivity.this,
                                "Order create failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }


    private void openPayPalInBrowser(String approveUrl) {
        try {
            androidx.browser.customtabs.CustomTabsIntent customTabsIntent =
                    new androidx.browser.customtabs.CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .build();
            customTabsIntent.launchUrl(MyWalletActivity.this, Uri.parse(approveUrl));
        } catch (Exception e) {
            // Fallback - normal browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(approveUrl));
            startActivity(browserIntent);
        }
    }


    private void capturePaypalOrder(String orderId) {
        customDialogClass.show();

        String baseUrl = isPaypalLive()
                ? "https://api-m.paypal.com"
                : "https://api-m.sandbox.paypal.com";

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                "{}",
                okhttp3.MediaType.parse("application/json")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl + "/v2/checkout/orders/" + orderId + "/capture")
                .header("Authorization", "Bearer " + paypalAccessToken)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(MyWalletActivity.this,
                            getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "null";
                Log.d("PAYPAL", "Capture response code: " + response.code());
                Log.d("PAYPAL", "Capture response body: " + responseBody);

                if (response.isSuccessful()) {
                    Log.d("PAYPAL", "✅ Payment Captured!");
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        callPurchaseApi(orderId, "PayPal");
                    });
                } else {
                    Log.e("PAYPAL", "❌ Capture failed: " + responseBody);
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(MyWalletActivity.this,
                                getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void launchPaypalPayment(DiamondPlanRoot.DiamondPlanItem plan) {

        fetchPaypalAccessToken(plan);
    }

    /**
     * Razorpay start
     **/


    private void launchRazorpayPayment(DiamondPlanRoot.DiamondPlanItem plan) {
        currentPlanId = plan.getId();

//        String razorpayKey = IS_RAZORPAY_LIVE ? RAZORPAY_LIVE_KEY : RAZORPAY_TEST_KEY;

        com.razorpay.Checkout checkout = new com.razorpay.Checkout();
        checkout.setKeyID(RAZORPAY_LIVE_KEY);
        checkout.setImage(R.drawable.app_logo);

        try {
            String currency = sessionManager.getSetting().getCurrency().getCurrencyCode();

            // ✅ Zero-decimal currencies list
            List<String> zeroDecimalCurrencies = Arrays.asList(
                    "JPY", "KRW", "VND", "IDR", "CLP", "PYG", "UGX", "XAF", "XOF"
            );


            double planAmount = (double) plan.getDollar();

            int amount;
            if (zeroDecimalCurrencies.contains(currency.toUpperCase())) {
                amount = (int) Math.round(planAmount);
            } else {
                amount = (int) Math.round(planAmount * 100);
            }

            Log.d("RAZORPAY", "Currency: " + currency + " | planAmount: " + planAmount + " | finalAmount: " + amount);

            org.json.JSONObject options = new org.json.JSONObject();
            options.put("name", getString(R.string.app_name));
            options.put("description", "Diamond Purchase - " + plan.getDiamonds() + " Diamonds");
            options.put("currency", currency);
            options.put("amount", amount);
            options.put("prefill.email", sessionManager.getUser().getEmail());
            options.put("prefill.contact", "");
            options.put("theme.color", "#360D46");

            checkout.open(MyWalletActivity.this, options);

        } catch (Exception e) {
            Log.e(TAG, "RazorPay error: " + e.getMessage());
            Toast.makeText(this, "Payment init failed", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Payment Success
    @Override
    public void onPaymentSuccess(String razorpayPaymentId,
                                 com.razorpay.PaymentData paymentData) {
        Log.d("RAZORPAY", "✅ Payment Success: " + razorpayPaymentId);
        callPurchaseApi(razorpayPaymentId, "RazorPay");
    }

    // ✅ Payment Failed
    @Override
    public void onPaymentError(int code, String description,
                               com.razorpay.PaymentData paymentData) {
        Log.e("RAZORPAY", "❌ Payment Failed: " + code + " - " + description);

        if (code == com.razorpay.Checkout.PAYMENT_CANCELED) {
            Toast.makeText(this, getString(R.string.purchase_canceled),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.purchase_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // CashFree

    private void launchCashfreePayment(DiamondPlanRoot.DiamondPlanItem plan) {
        currentPlanId = plan.getId();
        customDialogClass.show();

        String appId = CASHFREE_LIVE_APP_ID;
        String secret =  CASHFREE_LIVE_SECRET;
//        String baseUrl = IS_CASHFREE_LIVE
//                ? "https://api.cashfree.com/pg/orders"
//                : "https://sandbox.cashfree.com/pg/orders";

        String baseUrl = getCashfreeBaseUrl();

        String currency = sessionManager.getSetting().getCurrency().getCurrencyCode();
        String amountStr = String.format(java.util.Locale.US, "%.2f", (double) plan.getDollar());
        String orderId = "order_" + System.currentTimeMillis();
        String userId = sessionManager.getUser().getId();
        String userEmail = sessionManager.getUser().getEmail();

        // ✅ Order create JSON
        String orderJson = "{"
                + "\"order_id\":\"" + orderId + "\","
                + "\"order_amount\":" + amountStr + ","
                + "\"order_currency\":\"" + currency + "\","
                + "\"customer_details\":{"
                + "\"customer_id\":\"" + userId + "\","
                + "\"customer_email\":\"" + userEmail + "\","
                + "\"customer_phone\":\"9999999999\""
                + "},"
                + "\"order_meta\":{"
                + "\"return_url\":\"https://yourapp.com/return?order_id={order_id}\""
                + "}"
                + "}";

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                orderJson,
                okhttp3.MediaType.parse("application/json")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl)
                .header("x-api-version", "2023-08-01")
                .header("x-client-id", appId)
                .header("x-client-secret", secret)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(MyWalletActivity.this,
                            "Cashfree init failed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Log.d("CASHFREE", "Order response: " + json);
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(json);
                        String paymentSessionId = obj.getString("payment_session_id");
                        String createdOrderId = obj.getString("order_id");

                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            openCashfreeCheckout(createdOrderId, paymentSessionId);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(MyWalletActivity.this,
                                    "Cashfree parse error", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    Log.e("CASHFREE", "Order failed: " + errorBody);
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(MyWalletActivity.this,
                                "Cashfree order failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void openCashfreeCheckout(String orderId, String paymentSessionId) {
        try {
            com.cashfree.pg.api.CFPaymentGatewayService.getInstance()
                    .setCheckoutCallback(MyWalletActivity.this);

            com.cashfree.pg.core.api.CFSession.Environment environment =
                    isCashfreeLive()
                            ? com.cashfree.pg.core.api.CFSession.Environment.PRODUCTION
                            : com.cashfree.pg.core.api.CFSession.Environment.SANDBOX;

            // ✅ Session
            com.cashfree.pg.core.api.CFSession cfSession =
                    new com.cashfree.pg.core.api.CFSession.CFSessionBuilder()
                            .setEnvironment(environment)
                            .setPaymentSessionID(paymentSessionId)
                            .setOrderId(orderId)
                            .build();

            // ✅ Theme - standalone object
            com.cashfree.pg.core.api.CFTheme cfTheme =
                    new com.cashfree.pg.core.api.CFTheme.CFThemeBuilder()
                            .setNavigationBarBackgroundColor("#360D46")
                            .setNavigationBarTextColor("#FFFFFF")
                            .setButtonBackgroundColor("#360D46")
                            .setButtonTextColor("#FFFFFF")
                            .setPrimaryTextColor("#000000")
                            .setSecondaryTextColor("#333333")
                            .build();

            // ✅ CFDropCheckoutPayment - theme અલગ method માં set
            com.cashfree.pg.ui.api.CFDropCheckoutPayment cfDropCheckoutPayment =
                    new com.cashfree.pg.ui.api.CFDropCheckoutPayment.CFDropCheckoutPaymentBuilder()
                            .setSession(cfSession)
                            .setCFNativeCheckoutUITheme(cfTheme)   // ✅ setCFTheme નહીં - setTheme
                            .build();

            com.cashfree.pg.api.CFPaymentGatewayService.getInstance()
                    .doPayment(MyWalletActivity.this, cfDropCheckoutPayment);

        } catch (com.cashfree.pg.core.api.exception.CFException e) {
            Log.e("CASHFREE", "Checkout error: " + e.getMessage());
            runOnUiThread(() ->
                    Toast.makeText(MyWalletActivity.this,
                            "Cashfree checkout failed", Toast.LENGTH_SHORT).show()
            );
        }
    }

    // ✅ CFCheckoutResponseCallback interface methods
    @Override
    public void onPaymentVerify(String orderId) {
        Log.d("CASHFREE", "✅ Payment verified: " + orderId);
        runOnUiThread(() -> callPurchaseApi(orderId, "Cashfree"));
    }

    @Override
    public void onPaymentFailure(
            com.cashfree.pg.core.api.utils.CFErrorResponse cfErrorResponse,
            String orderId) {
        Log.e("CASHFREE", "❌ Payment failed: " + cfErrorResponse.getMessage());
        runOnUiThread(() -> {
            if ("USER_DROPPED".equals(cfErrorResponse.getStatus())) {
                Toast.makeText(MyWalletActivity.this,
                        getString(R.string.purchase_canceled), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MyWalletActivity.this,
                        getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //paystack

    private void launchPaystackPayment(DiamondPlanRoot.DiamondPlanItem plan) {
        currentPlanId = plan.getId();
        customDialogClass.show();

//        String secretKey = IS_PAYSTACK_LIVE
//                ? PAYSTACK_LIVE_SECRET_KEY
//                : PAYSTACK_TEST_SECRET_KEY;

        String secretKey = PAYSTACK_LIVE_SECRET_KEY;

        String email = sessionManager.getUser().getEmail();

        // ✅ Paystack = NGN only (sandbox), Live account માં other currencies enable કરી શકો
        String paystackCurrency = isPaystackLive()
                ? sessionManager.getSetting().getCurrency().getCurrencyCode()
                : "NGN"; // ✅ Sandbox = always NGN

        // ✅ Amount × 100 (kobo)
        int amountInSubunit = (int) Math.round((double) plan.getDollar() * 100);

        String initJson = "{"
                + "\"email\":\"" + email + "\","
                + "\"amount\":" + amountInSubunit + ","
                + "\"currency\":\"" + paystackCurrency + "\","
                + "\"reference\":\"pay_" + System.currentTimeMillis() + "\","
                + "\"callback_url\":\"https://paystack.com/close\""  // ✅ add
                + "}";

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                initJson,
                okhttp3.MediaType.parse("application/json")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.paystack.co/transaction/initialize")
                .header("Authorization", "Bearer " + secretKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(MyWalletActivity.this,
                            "Paystack init failed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Log.d("PAYSTACK", "Init response: " + json);
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(json);
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
                            Toast.makeText(MyWalletActivity.this,
                                    "Paystack parse error", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    Log.e("PAYSTACK", "Init failed: " + errorBody);
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(MyWalletActivity.this,
                                "Paystack init failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void openPaystackCheckout(String accessCode, String reference) {
        String checkoutUrl = "https://checkout.paystack.com/" + accessCode;

        Intent intent = new Intent(MyWalletActivity.this,
                PaystackWebViewActivity.class);
        intent.putExtra(PaystackWebViewActivity.EXTRA_URL, checkoutUrl);
        intent.putExtra(PaystackWebViewActivity.EXTRA_REFERENCE, reference);
        paystackLauncher.launch(intent);
    }


    private void verifyPaystackPayment(String reference) {
        customDialogClass.show();

//        String secretKey = IS_PAYSTACK_LIVE
//                ? PAYSTACK_LIVE_SECRET_KEY
//                : PAYSTACK_TEST_SECRET_KEY;

        String secretKey = PAYSTACK_LIVE_SECRET_KEY;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.paystack.co/transaction/verify/" + reference)
                .header("Authorization", "Bearer " + secretKey)
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(MyWalletActivity.this,
                            getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Log.d("PAYSTACK", "Verify response: " + json);
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(json);
                        org.json.JSONObject data = obj.getJSONObject("data");
                        String status = data.getString("status");

                        if ("success".equals(status)) {
                            // ✅ Payment confirmed - backend notify
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                callPurchaseApi(reference, "Paystack");
                            });
                        } else {
                            // canceled or failed
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();

                                Toast.makeText(MyWalletActivity.this,
                                        getString(R.string.purchase_canceled),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(MyWalletActivity.this,
                                    getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(MyWalletActivity.this,
                                getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });

    }

    private void callPurchaseApi(String paymentId, String paymentgateway) {
        customDialogClass.show();
        JsonObject payload = new JsonObject();
        payload.addProperty("userId", sessionManager.getUser().getId());
        payload.addProperty("planId", currentPlanId);
        payload.addProperty("currency", paymentgateway);
        payload.addProperty("payment_intent_id", paymentId);

        RetrofitBuilder.create().purchasePlanStripeDiamonds(payload)
                .enqueue(new Callback<UserRoot>() {
                    @Override
                    public void onResponse(@NonNull Call<UserRoot> call,
                                           @NonNull Response<UserRoot> response) {
                        if (!isUiAlive()) return;
                        customDialogClass.dismiss();
                        Log.d("RAZORPAY", "Backend response: " + response.code());
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isStatus()) {
                            sessionManager.saveUser(response.body().getUser());
                            binding.tvMyCoins.setText(
                                    RayziUtils.formatCoin(response.body().getUser().getDiamond())
                            );
                            Toast.makeText(MyWalletActivity.this,
                                    getString(R.string.purchase_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyWalletActivity.this,
                                    getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserRoot> call, @NonNull Throwable t) {
                        if (!isUiAlive()) return;
                        customDialogClass.dismiss();
                        Log.e(TAG, "RazorPay backend error", t);
                        Toast.makeText(MyWalletActivity.this,
                                getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void launchFlutterwavePayment(DiamondPlanRoot.DiamondPlanItem plan) {
        currentPlanId = plan.getId();
        customDialogClass.show();

        String currency = sessionManager.getSetting().getCurrency().getCurrencyCode();
        String email = sessionManager.getUser().getEmail();
        String name = sessionManager.getUser().getName();
        String txRef = "flw_" + System.currentTimeMillis();
        String amountStr = String.format(java.util.Locale.US, "%.2f", (double) plan.getDollar());

        // ✅ Flutterwave Hosted Payment Link generate
        // Payment page URL format
        String checkoutUrl = "https://checkout.flutterwave.com/v3/hosted/pay";

        // ✅ Build payment JSON - POST to Flutterwave API
        String paymentJson = "{"
                + "\"tx_ref\":\"" + txRef + "\","
                + "\"amount\":\"" + amountStr + "\","
                + "\"currency\":\"" + currency + "\","
                + "\"redirect_url\":\"https://flutterwave.com/pay/close\","
                + "\"customer\":{"
                + "\"email\":\"" + email + "\","
                + "\"name\":\"" + name + "\""
                + "},"
                + "\"customizations\":{"
                + "\"title\":\"" + getString(R.string.app_name) + "\","
                + "\"description\":\"Diamond Purchase\","
                + "\"logo\":\"\""
                + "}"
                + "}";

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                paymentJson,
                okhttp3.MediaType.parse("application/json")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.flutterwave.com/v3/payments")
                .header("Authorization", "Bearer " + FLUTTERWAVE_SECRET_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(MyWalletActivity.this,
                            getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Log.d("FLW", "Payment link response: " + json);
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(json);
                        String status = obj.getString("status");
                        if ("success".equals(status)) {
                            String paymentLink = obj.getJSONObject("data").getString("link");
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                openFlutterwaveCheckout(paymentLink, txRef);
                            });
                        } else {
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                Toast.makeText(MyWalletActivity.this,
                                        getString(R.string.something_went_wrong_text),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(MyWalletActivity.this,
                                    getString(R.string.something_went_wrong_text),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    Log.e("FLW", "Payment link failed: " + errorBody);

                    // ✅ Currency error detect
                    final String msg = errorBody.toLowerCase().contains("currency")
                            ? getString(R.string.currency_not_supported)
                            : getString(R.string.something_went_wrong_text);

                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(MyWalletActivity.this, msg, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void openFlutterwaveCheckout(String paymentLink, String txRef) {
        Intent intent = new Intent(MyWalletActivity.this,
                com.codder.ultimate.activity.FlutterwaveWebViewActivity.class);
        intent.putExtra(FlutterwaveWebViewActivity.EXTRA_URL, paymentLink);
        intent.putExtra(FlutterwaveWebViewActivity.EXTRA_REFERENCE, txRef);
        flutterwaveLauncher.launch(intent);
    }

    private void verifyFlutterwavePayment(String txRef) {
        customDialogClass.show();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.flutterwave.com/v3/transactions/verify_by_reference?tx_ref=" + txRef)
                .header("Authorization", "Bearer " + FLUTTERWAVE_SECRET_KEY)
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    customDialogClass.dismiss();
                    Toast.makeText(MyWalletActivity.this,
                            getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Log.d("FLW", "Verify response: " + json);
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(json);
                        org.json.JSONObject data = obj.getJSONObject("data");
                        String status = data.getString("status");

                        if ("successful".equals(status)) {
                            // ✅ Payment confirmed
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                callPurchaseApi(txRef, "Flutterwave");
                            });
                        } else {
                            runOnUiThread(() -> {
                                customDialogClass.dismiss();
                                Toast.makeText(MyWalletActivity.this,
                                        getString(R.string.purchase_canceled),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            customDialogClass.dismiss();
                            Toast.makeText(MyWalletActivity.this,
                                    getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        customDialogClass.dismiss();
                        Toast.makeText(MyWalletActivity.this,
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

    private boolean isUiAlive() {
        return binding != null && !isFinishing() && !isDestroyed();
    }

    /** Disconnect BillingClient and clean up binding **/
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection(); //
        }
        binding = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPaypal();
    }
}
