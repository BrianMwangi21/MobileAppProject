package iamprogrammer.brian.com.stutor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;


public class SignInActivity extends AppCompatActivity {

    private EditText emailET, passET;
    private ActionProcessButton signinBtn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Init mAuth
        mAuth = FirebaseAuth.getInstance();

        // Check
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    Toast.makeText( SignInActivity.this, "User found", Toast.LENGTH_SHORT ).show();
                } else {
                    Toast.makeText( SignInActivity.this, "User not found", Toast.LENGTH_SHORT ).show();
                }

            }
        };

        // For google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Find views
        emailET = findViewById(R.id.edittext_email);
        passET = findViewById(R.id.edittext_password);
        signinBtn = findViewById(R.id.button_signin);
        signinBtn.setMode( ActionProcessButton.Mode.ENDLESS );

        // On click
        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signinBtn.setProgress(1);
                signInUser();
            }
        });

        // For google sign in
        SignInButton signInButton = findViewById(R.id.button_signinwithgoogle);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void signInUser() {
        // Get info
        String email = emailET.getText().toString();
        String pass = passET.getText().toString();

        if( !email.isEmpty() && !pass.isEmpty() ) {
            mAuth.signInWithEmailAndPassword( email, pass ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if( task.isComplete() ){
                        Toast.makeText( SignInActivity.this, "Task complete", Toast.LENGTH_SHORT).show();

                        if( task.isSuccessful() ) {
                            signinBtn.setProgress(100);
                            Toast.makeText( SignInActivity.this, "Task successful", Toast.LENGTH_SHORT ).show();
                        }else {
                            signinBtn.setProgress(-1);
                            Toast.makeText( SignInActivity.this, "Task unsuccessful", Toast.LENGTH_SHORT ).show();
                        }
                    }
                }
            });
        }else {
            Toast.makeText( this, "Fill all fields", Toast.LENGTH_SHORT ).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Toast.makeText( this, account.getDisplayName(), Toast.LENGTH_SHORT ).show();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText( this, "Failed miserably", Toast.LENGTH_SHORT).show();
        }
    }
}
