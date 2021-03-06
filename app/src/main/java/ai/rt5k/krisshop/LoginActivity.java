package ai.rt5k.krisshop;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    MainApplication m;

    EditText edtUsername, edtPassword;
    Button btnLogin;
    TextView txtTitle, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        m = (MainApplication) getApplicationContext();
        if(m.mainQueue == null) {
            m.mainQueue = Volley.newRequestQueue(LoginActivity.this);
        }

        txtTitle = findViewById(R.id.txtTitle);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        String s = txtTitle.getText().toString();
        SpannableString ss1 = new SpannableString(s);
        ss1.setSpan(new RelativeSizeSpan(0.9f), 1,4, 0); // set size
        ss1.setSpan(new RelativeSizeSpan(0.9f), 5,8, 0); // set size
        txtTitle.setText(ss1);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("LoginActivity", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        final String token = task.getResult().getToken();
                        Log.d("LoginActivity", token);

                        StringRequest loginRequest = new StringRequest(Request.Method.POST,MainApplication.SERVER_URL + "/login", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("LoginActvity", response);
                                try {
                                    JSONObject responseObject = new JSONObject(response);

                                    if(responseObject.getBoolean("success")) {
                                        m.sessionId = responseObject.getString("session_id");
                                        m.uid = edtUsername.getText().toString().substring(1);
                                        m.name = responseObject.getString("name");
                                        if(responseObject.getString("auth").equals("user")) {
                                            Intent customerHomeIntent = new Intent(LoginActivity.this, CustomerHomeActivity.class);
                                            startActivity(customerHomeIntent);
                                            finish();
                                        }
                                        else {
                                            Intent employeeHomeIntent = new Intent(LoginActivity.this, EmHomeActivity.class);
                                            startActivity(employeeHomeIntent);
                                            finish();
                                        }
                                    }
                                    else {
                                        Snackbar.make(btnLogin, "Login failed: " + responseObject.getString("message"), Snackbar.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("LoginActivity", error.toString());
                            }
                        }){
                            @Override
                            protected Map<String,String> getParams(){
                                Map<String,String> params = new HashMap<String, String>();
                                params.put("mem_id", edtUsername.getText().toString());
                                params.put("password", edtPassword.getText().toString());
                                params.put("fcm_token", token);
                                return params;
                            }
                        };

                        m.mainQueue.add(loginRequest);
                    }
                });
            }
        });
    }
}
