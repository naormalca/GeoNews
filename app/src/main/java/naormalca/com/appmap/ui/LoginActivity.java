package naormalca.com.appmap.ui;

import android.content.Intent;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import naormalca.com.appmap.MainActivity;
import naormalca.com.appmap.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName() ;
    @BindView(R.id.emailEditText) EditText mEmailEditText;
    @BindView(R.id.passwordEditText) EditText mPasswordEditText;
    @BindView(R.id.signInBtn) Button mSignInBtn;
    @BindView(R.id.signUpBtn) Button mSignUpBtn;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();


    }
    @OnClick(R.id.signInBtn)
    public void signIn(View view){
        if (validateForm()) {
            // Hide sign-in button
            mSignInBtn.setVisibility(View.GONE);
            mSignUpBtn.setVisibility(View.GONE);

            String email = mEmailEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success
                                Log.d(TAG, "signInWithEmail:success");
                                Toast.makeText(LoginActivity.this, "התחברת בהצלחה!",
                                        Toast.LENGTH_SHORT).show();
                                finish();


                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                mSignInBtn.setVisibility(View.VISIBLE);
                                mSignUpBtn.setVisibility(View.VISIBLE);
                            }

                        }
                    });
        }
    }

    private boolean validateForm() {
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        if (email.isEmpty()){
            mEmailEditText.setError("Email is required");
            mEmailEditText.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mEmailEditText.setError("Please enter a valid email");
            mEmailEditText.requestFocus();
            return false;
        }
        if (password.isEmpty()){
            mPasswordEditText.setError("Password is required");
            mPasswordEditText.requestFocus();
            return false;
        }
        if (password.length() < 6){
            mPasswordEditText.setError("Minimum length of password should be 6");
            mPasswordEditText.requestFocus();
            return false;
        }
        return true;
    }

    @OnClick(R.id.signUpBtn)
    public void openSignUpActivity(View view){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class );
        startActivity(intent);
    }
}
