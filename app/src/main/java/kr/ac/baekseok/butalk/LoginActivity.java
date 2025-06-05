package kr.ac.baekseok.butalk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class LoginActivity extends AppCompatActivity {

    private Spinner spinnerUserType;
    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnGoogleLogin, btnDeviceAuth;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase 인증 초기화
        mAuth = FirebaseAuth.getInstance();

        // XML 요소와 연결
        spinnerUserType = findViewById(R.id.spinnerUserType);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnDeviceAuth = findViewById(R.id.btnDeviceAuth);  // 기기 인증 더미 버튼

        // Spinner 동작 (선택값 확인용)
        spinnerUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String userType = parent.getItemAtPosition(position).toString();
                // 필요 시 사용 가능
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 일반 로그인은 안내 메시지
        btnLogin.setOnClickListener(v -> {
            Toast.makeText(this, "아직 연동 전입니다.\n우선은 구글 계정으로 로그인해주세요.", Toast.LENGTH_SHORT).show();
        });

        // 기기 인증 버튼은 더미
        btnDeviceAuth.setOnClickListener(v -> {
            Toast.makeText(this, "기기 인증은 현재 지원하지 않습니다. 구글 인증을 이용하시면 기기인증 없이 사용가능합니다.", Toast.LENGTH_SHORT).show();
        });

        // Google 로그인 옵션 구성
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google 로그인 버튼 동작
        btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    // 구글 로그인 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google 로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 구글 계정으로 Firebase 인증
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Google 로그인 성공", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MenuActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firebase 인증 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
