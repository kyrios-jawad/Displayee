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
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kyrios.displayee.Model.User;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout username, fullname, email, password;
    private Button register, already_have_an_acc;
    private ImageView back;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private ProgressDialog pd;
    private int flag = 1;
    private static int usernameFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.inputuser);
        fullname = findViewById(R.id.inputname);
        email = findViewById(R.id.inputemail);
        password = findViewById(R.id.inputpassword);
        register = findViewById(R.id.registerbtn);

        auth = FirebaseAuth.getInstance();

        already_have_an_acc = (Button) findViewById(R.id.already_account_button);
        already_have_an_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this.getApplicationContext(), LoginActivity.class);
                RegisterActivity.this.startActivity(intent);
                RegisterActivity.this.finish();
            }
        });
        back = (ImageView) findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this.getApplicationContext(), welcome.class);
                RegisterActivity.this.startActivity(intent);
                RegisterActivity.this.finish();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validate_email() | !validate_password() | !validate_name()) {


                    return;
                } else {
                    checkUsernameAvailability();
                    if (flag == 0) {
                        return;
                    } else {
                        pd = new ProgressDialog(RegisterActivity.this);
                        pd.setMessage("Please wait...");
                        pd.show();
                        String str_username = username.getEditText().getText().toString();
                        String str_fullname = fullname.getEditText().getText().toString();
                        String str_email = email.getEditText().getText().toString();
                        String str_password = password.getEditText().getText().toString();
                        register(str_username, str_fullname, str_email, str_password);
                    }
                }

            }
        });

    }

    private void checkUsernameAvailability() {

        final String txt_username = username.getEditText().getText().toString();
        DatabaseReference mUsersRed = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersRed.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (txt_username.equals(user.getUsername())) {
                        Toast.makeText(RegisterActivity.this, "Username alredy taken! Try something else.", Toast.LENGTH_SHORT).show();
                        flag = 0;
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void register(final String username, final String fullname, String email, String password) {
        if (usernameFlag == 0) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        String userid = firebaseUser.getUid();

                        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("id", userid);
                        hashMap.put("username", username.toLowerCase());
                        hashMap.put("fullname", fullname);
                        hashMap.put("bio", "");
                        hashMap.put("imageurl", "default");

                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    pd.dismiss();
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }
                        });

                    } else {
                        pd.dismiss();
                        Toast.makeText(RegisterActivity.this, "You can't register with this email and password!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            pd.dismiss();
            Toast.makeText(this, "Please change the username!", Toast.LENGTH_SHORT).show();
        }
    }

    // Validation's
    // email
    private Boolean validate_email() {
        String val_email = email.getEditText().getText().toString();
        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

        Pattern pattern = Pattern.compile(regex);
        String val_pat = pattern.toString();
        if (val_email.isEmpty()) {
            email.setError("this field cannot be empty");
            return false;
        } else if (!val_email.matches(val_pat)) {
            email.setError("Invalid Email");
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }

    // name
    private Boolean validate_name() {
        String val_name = username.getEditText().getText().toString();
        if (val_name.isEmpty()) {
            username.setError("this field cannot be empty");
            return false;
        } else {
            username.setError(null);
            return true;
        }
    }

    // password
    private Boolean validate_password() {
        String val_password = password.getEditText().getText().toString();
        if (val_password.isEmpty()) {
            password.setError("this field cannot be empty");
            return false;
        } else if (val_password.length() <= 6) {
            password.setError("password should be more than 6 character's");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        ;
    }


}
