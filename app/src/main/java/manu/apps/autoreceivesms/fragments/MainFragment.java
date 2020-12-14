package manu.apps.autoreceivesms.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import manu.apps.autoreceivesms.R;

public class MainFragment extends Fragment {

    MainViewModel mainViewModel;

    TextView tvReceivedSms;

    NavController navController;

    BroadcastReceiver broadcastReceiver;

    TextView tvPrivacy_Policy;

    public static final int RECEIVE_SMS_REQUEST = 1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        tvReceivedSms = view.findViewById(R.id.tv_received_sms);

        tvPrivacy_Policy.setOnClickListener(v -> {

            final ProgressDialog progressDialog = new ProgressDialog(requireActivity());
            progressDialog.setMessage("Loading.......");
            progressDialog.show();
            progressDialog.setCancelable(false);

            android.app.AlertDialog.Builder alert = new AlertDialog.Builder(requireActivity());

            WebView webView = new WebView(requireActivity());
            webView.loadUrl("https://dash.dohyangu.ke/app/apis/privacyPolicy.html");
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    // Do something when the page starts
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progressDialog.dismiss();
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });


            alert.setView(webView);
            alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                    dialog.dismiss();

                }
            });
            alert.show();

        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            {
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
                    // If The Permission has already been granted proceed with the action

                    Toast.makeText(requireActivity(), "SMS Permission has been granted", Toast.LENGTH_SHORT).show();

                    //receiveOTP();

                    readOTP();

                } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS)) {

                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected. In this UI,
                    // include a "cancel" or "no thanks" button that allows the user to
                    // continue using your app without granting the permission.

                    showActionDialog("We require SMS permission to be able to read the OTP you will receive and " +
                                    "validate your phone number, you will be directed to login on Cancel",
                            "Cancel", "onCancelClicked",
                            ContextCompat.getDrawable(requireActivity(), R.drawable.ic_redirect));

                } else {

                    // If Permission has not been granted
                    // Request the permission first time

                    // The registered ActivityResultCallback gets the result of this request.

                    requestPermissionLauncher.launch(
                            Manifest.permission.RECEIVE_SMS);


                }
            }
        }


    }

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

                    // Permission is granted. Continue the action or workflow in your
                    // app.

                    //receiveOTP();

                    readOTP();

                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.

                    showActionDialog("You won't be able to proceed because SMS permission " +
                                    "is required to validate your phone number, you will be directed to login on Close",
                            "Close", "onCloseClicked",
                            ContextCompat.getDrawable(requireActivity(), R.drawable.ic_redirect));

                }
            });


    private void readOTP() {

        IntentFilter smsReceivedFilter = new IntentFilter();
        smsReceivedFilter.addAction("android.provider.Telephony.SMS_RECEIVED");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle pduBundle = intent.getExtras();

                /* Don't change pduBundle.get("pdus") to anything else it will return protocolDataUnit
                Object[] array as null */
                Object[] protocolDataUnit = (Object[]) pduBundle.get("pdus");
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) protocolDataUnit[0]);

                String sender = smsMessage.getOriginatingAddress();
                String messageBody = smsMessage.getMessageBody();

                tvReceivedSms.setText(messageBody);

//                if (sender.equalsIgnoreCase("DohYangu")) {
//
//
//
//                    String regex = "\\d+";
//                    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
//                    Matcher matcher = pattern.matcher(messageBody);
//
//                    if (matcher.find()) {
//
//                        new AlertDialog.Builder(context)
//                                .setMessage("Your OTP is: " + matcher.group(0))
//                                .show();
//
//                    }
//
//
//                }

            }
        };

        requireActivity().registerReceiver(broadcastReceiver, smsReceivedFilter);

    }

    private void receiveOTP() {

        ProgressDialog progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setMessage("loading");
        progressDialog.show();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://dev.dohyangu.ke/app/apis/check_phone.php",
                response -> {

                    progressDialog.dismiss();

                    try {

                        JSONObject jsonObject = new JSONObject(response);

                        String checkPhoneStatus = jsonObject.getString("success");
                        String checkPhoneMessage = jsonObject.getString("message");

                        JSONArray jsonArray = jsonObject.getJSONArray("checkPhoneNumber");

                        if (checkPhoneStatus.equals("1")) {
                            for (int i = 0; i < jsonArray.length(); i++) {

                                Toast.makeText(requireActivity(), checkPhoneMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (checkPhoneStatus.equals("0")) {

                            Toast.makeText(requireActivity(), checkPhoneMessage, Toast.LENGTH_SHORT).show();


                        } else if (checkPhoneStatus.equals("2")) {

                            Toast.makeText(requireActivity(), checkPhoneMessage, Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {

                        e.printStackTrace();

                        progressDialog.dismiss();


                        Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {

                    progressDialog.dismiss();


                    Toast.makeText(requireActivity(), error.toString(), Toast.LENGTH_SHORT).show();

                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("phoneNumber", "0714810492");

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);

    }

//    private void receiveSMS() {
//
//        ProgressDialog progressDialog = new ProgressDialog(requireActivity());
//        progressDialog.setMessage("loading");
//        progressDialog.show();
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://dev.dohyangu.ke/app/apis/forgotpin.php",
//                response -> {
//
//                    progressDialog.dismiss();
//
//                    try {
//
//                        JSONObject jsonObject = new JSONObject(response);
//                        String resetPinStatus = jsonObject.getString("status");
//                        String resetPinMessage = jsonObject.getString("message");
//
//                        if (resetPinStatus.equals("1")) {
//
//                            Toast.makeText(requireActivity(), resetPinMessage, Toast.LENGTH_LONG).show();
//
//                        } else if (resetPinStatus.equals("0")) {
//
//                            Toast.makeText(requireActivity(), resetPinMessage, Toast.LENGTH_LONG).show();
//
//                        }
//
//                    } catch (JSONException e) {
//
//                        e.printStackTrace();
//
//                        Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
//
//                    }
//                },
//                error -> {
//
//                    error.printStackTrace();
//
//                    progressDialog.dismiss();
//
//                    Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
//
//
//                }) {
//            @Override
//            protected Map<String, String> getParams() {
//
//                Map<String, String> params = new HashMap<>();
//                params.put("phoneNumber", "0714810492");
//
//                return params;
//
//            }
//        };
//
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
//        requestQueue.add(stringRequest);
//
//    }

    public void showActionDialog(final String tvMessage, String btnText, String btnCommand, Drawable imvDrawable) {

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.layout_action_dialog);
        dialog.show();
        dialog.setCancelable(false);

        // Setting dialog background to transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        // Setting size of the dialog
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        ImageView imvActionDialog = dialog.findViewById(R.id.imv_action_dialog);
        TextView tvActionDialog = dialog.findViewById(R.id.tv_action_dialog_message);
        MaterialButton btnActionDialog = dialog.findViewById(R.id.btn_action_dialog);


        imvActionDialog.setImageDrawable(imvDrawable);
        tvActionDialog.setText(tvMessage);
        btnActionDialog.setText(btnText);

        if (btnCommand.equalsIgnoreCase("onCloseClicked")) {
            btnActionDialog.setOnClickListener(v -> {

                dialog.dismiss();

            });
        } else if (btnCommand.equalsIgnoreCase("onCancelClicked")) {
            btnActionDialog.setOnClickListener(v -> {


                dialog.dismiss();

            });
        }

    }

    @Override
    public void onStop() {

        if (broadcastReceiver != null) {
            requireActivity().unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }

        Log.d("MainFragmentOnStop", "onStop has been called");

        super.onStop();
    }

    @Override
    public void onDestroy() {

        if (broadcastReceiver != null) {
            requireActivity().unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }

        Log.d("MainFragmentOnDestroy", "onDestroy has been called");

        super.onDestroy();
    }
}