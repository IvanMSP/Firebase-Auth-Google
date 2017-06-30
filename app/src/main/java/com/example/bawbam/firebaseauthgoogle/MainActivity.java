package com.example.bawbam.firebaseauthgoogle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener{

    private static final int SIGN_IN_GOOGLE_CODE = 1;
    private static final String TAG ="MainActivity" ;
    private SignInButton btnSignGoogle;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignGoogle = (SignInButton) findViewById(R.id.btnSignGoogle);
        Initialize();


        btnSignGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, SIGN_IN_GOOGLE_CODE);
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
                    Log.w(TAG, "onAuthStateChanged - signed_in" + firebaseUser.getUid());
                    Log.w(TAG, "onAuthStateChanged - signed_in" + firebaseUser.getEmail());
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

    private void signInGoogleFirebase(GoogleSignInResult googleSignResult){
            if(googleSignResult.isSuccess()){
                AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignResult.getSignInAccount().getIdToken(),null);
                firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this,"authentication success", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(MainActivity.this, FullStackActivity.class);
                            startActivity(i);
                            finish();
                        }else{
                            Toast.makeText(MainActivity.this,"authentication Unsuccess", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else{
                Toast.makeText(MainActivity.this,"Google Sign In  Unsuccess", Toast.LENGTH_LONG).show();
            }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_GOOGLE_CODE){
            GoogleSignInResult googleSignResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);//Aqui se hace el SignIn
            signInGoogleFirebase(googleSignResult);
        }
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
