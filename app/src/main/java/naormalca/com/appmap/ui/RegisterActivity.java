package naormalca.com.appmap.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import naormalca.com.appmap.Firebase.FirebaseDB;
import naormalca.com.appmap.MainActivity;
import naormalca.com.appmap.R;
import naormalca.com.appmap.misc.utils;
import naormalca.com.appmap.model.Users;

import static naormalca.com.appmap.misc.utils.parseFullName;

public class RegisterActivity extends AppCompatActivity
implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private Button signUpButton;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;
//1.At navigation bar every click load all DB
    //2.When signUp failed the button visible not back
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_activty);

        signUpButton = findViewById(R.id.signUpButton);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        signUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.signUpButton) {
            registerUser();
        }
    }

    //TODO: Split this function to validation and signup
    private void registerUser() {
        final String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Minimum length of password should be 6");
            passwordEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        signUpButton.setVisibility(View.GONE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                    String fullName = nameEditText.getText().toString();
                    // Split the fullName to first and last name
                    String[] nameArray = utils.parseFullName(fullName);
                    // Create new user
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Users newUser = new Users(nameArray[0],
                            nameArray.length == 1 ? null : nameArray[1], email, user.getUid());
                    // Get current user ID & push to database
                    mDatabase.child(FirebaseDB.USERS_DB).child(user.getUid()).setValue(newUser);
                    finish();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                } else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
    });
        progressBar.setVisibility(View.GONE);
        signUpButton.setVisibility(View.VISIBLE);
    }
}
