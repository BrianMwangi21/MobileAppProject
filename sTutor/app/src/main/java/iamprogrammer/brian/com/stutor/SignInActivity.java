package iamprogrammer.brian.com.stutor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignInActivity extends AppCompatActivity {

    private EditText emailET, passET;
    private String userType = "student";
    private ActionProcessButton signinBtn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 21;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Init mAuth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if( user != null ) {
                    // Nothing
                }else {
                    // Nothing too
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
            // Manual sign-up
            sTutor stutor = new sTutor(email, pass, userType);
            writeNewUser(stutor);
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

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            sTutor stutor = new sTutor( account.getEmail(), account.getId(), userType );
            writeNewUser(stutor);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText( this, "Ooops. Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    public void writeNewUser(sTutor stutor) {
        // Init firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        String userID = mDatabase.push().getKey();

        mDatabase.child(userID).setValue(stutor, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if( databaseError == null ) {
                    Toast.makeText( SignInActivity.this, "Done!", Toast.LENGTH_SHORT ).show();
                    signinBtn.setProgress(100);
                }else {
                    Toast.makeText( SignInActivity.this, "Oooops!", Toast.LENGTH_SHORT ).show();
                    signinBtn.setProgress(-1);
                }
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_student:
                if (checked)
                    userType = "student";
                    break;
            case R.id.radio_tutor:
                if (checked)
                    // Ninjas rule
                    userType = "tutor";
                    break;
        }
    }
}
