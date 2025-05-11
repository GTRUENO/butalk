package kr.ac.baekseok.butalk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    private TextView txtEmail;              // 로그인된 이메일 표시용
    private EditText edtNickname, edtPassword;
    private Button btnUpdateNickname, btnUpdatePassword, btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        txtEmail = findViewById(R.id.txtEmail); // 고정 텍스트로 이메일 표시
        edtNickname = findViewById(R.id.edtNickname);
        edtPassword = findViewById(R.id.edtPassword);
        btnUpdateNickname = findViewById(R.id.btnUpdateNickname);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        btnLogout = findViewById(R.id.btnLogout);

        // 로그인된 이메일 표시
        if (mAuth.getCurrentUser() != null) {
            txtEmail.setText("현재 로그인: " + mAuth.getCurrentUser().getEmail());
        }

        // 닉네임 변경
        btnUpdateNickname.setOnClickListener(v -> {
            String newNickname = edtNickname.getText().toString().trim();
            if (newNickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();
            db.getReference("users").child(uid).child("nickname").setValue(newNickname)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "닉네임이 변경되었습니다", Toast.LENGTH_SHORT).show()
                    );
        });

        // 비밀번호 변경
        btnUpdatePassword.setOnClickListener(v -> {
            String newPassword = edtPassword.getText().toString().trim();
            if (newPassword.length() < 6) {
                Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.getCurrentUser().updatePassword(newPassword)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "변경 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // 로그아웃
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
