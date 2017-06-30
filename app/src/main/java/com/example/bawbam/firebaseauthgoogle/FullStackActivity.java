package com.example.bawbam.firebaseauthgoogle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class FullStackActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "FullStackActivity";
    private Button btnSignOut;
    private TextView tvUserDetail;
    private ImageView imgPhoto;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_stack);

        tvUserDetail = (TextView)findViewById(R.id.tvUserDetail);
        btnSignOut = (Button) findViewById(R.id.btnSignOut);
        imgPhoto = (ImageView)findViewById(R.id.imgPhoto);
        Initialize();

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private  void signOut(){
        firebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    Intent i = new Intent(FullStackActivity.this,MainActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(FullStackActivity.this,"Error en la Operación",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void Initialize(){
        firebaseAuth = FirebaseAuth.getInstance();
        //este listener detecta los cambios en el inicio de Sesión
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser(); //Aqui cachamos todos los datos del user
                if(firebaseUser != null){
                    tvUserDetail.setText("IDUser:" + firebaseUser.getUid() + "Email:" + firebaseUser.getEmail());
                    Picasso.with(FullStackActivity.this).load(firebaseUser.getPhotoUrl()).into(imgPhoto);
                }else{
                    Log.w(TAG, "onAuthStateChanged - signed_out");
                }
            }
        };
        //Iniciar de Google Account

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)//necesario implementar  OnConnectionFailed de GoogleApiClient
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
