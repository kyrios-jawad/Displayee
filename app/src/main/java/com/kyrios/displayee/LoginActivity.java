package com.kyrios.displayee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private Button login,haveanaccount;
    private ImageView back;
    private TextInputLayout input_email, input_password;
    private ProgressDialog pd;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // hooks
        pd = new ProgressDialog(this);
        input_email = findViewById(R.id.inputemail_);
        input_password = findViewById(R.id.inputpassword_);
        mAuth = FirebaseAuth.getInstance();
        haveanaccount = findViewById(R.id.already_account_button);
        haveanaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this.getApplicationContext(), RegisterActivity.class);
                LoginActivity.this.startActivity(intent);
                LoginActivity.this.finish();
            }
        });
        back = findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this.getApplicationContext(), welcome.class);
                LoginActivity.this.startActivity(intent);
                LoginActivity.this.finishAffinity();
            }
        });
        login = findViewById(R.id.loginbtn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LoginActivity.this.validate_email() | !LoginActivity.this.validate_password()) {
                    return;
                } else {
                    pd.setMessage("Wait..");
                    pd.show();
                    String val_email = input_email.getEditText().getText().toString();
                    String val_password = input_password.getEditText().getText().toString();
                    LoginActivity.this.loginuser(val_email, val_password);
                }
            }
        });
    }
    //   login
    private void loginuser(String val_email, String val_password) {
        mAuth.signInWithEmailAndPassword(val_email, val_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    pd.dismiss();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        });

    }

    // Validation'
    // email
    private Boolean validate_email() {
        String val_email = input_email.getEditText().getText().toString();
        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

        Pattern pattern = Pattern.compile(regex);
        String val_pat = pattern.toString();
        if (val_email.isEmpty()) {
            input_email.setError("this field cannot be empty");
            return false;
        } else if (!val_email.matches(val_pat)) {
            input_email.setError("Invalid Email");
            return false;
        } else {
            input_email.setError(null);
            return true;
        }
    }

    // password
    private Boolean validate_password() {
        String val_password = input_password.getEditText().getText().toString();
        if (val_password.isEmpty()) {
            input_password.setError("this field cannot be empty");
            return false;
        } else if (val_password.length() <= 6) {
            input_password.setError("password should be more than 6 character's");
            return false;
        } else {
            input_password.setError(null);
            return true;
        }
    }

    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){

            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        };
    }
}