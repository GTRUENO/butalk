package kr.ac.baekseok.butalk;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class SettingsActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView nicknameText;
    private Button btnChangeNickname, btnChangeProfileImage, btnChangePassword, btnLogout;
    private BottomNavigationView bottomNavigation;
    private Uri selectedImageUri;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private FirebaseUser user;

    ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileImage = findViewById(R.id.profileImage);
        nicknameText = findViewById(R.id.nicknameText);
        btnChangeNickname = findViewById(R.id.btnChangeNickname);
        btnChangeProfileImage = findViewById(R.id.btnChangeProfileImage);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        loadUserProfile();

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        uploadProfileImage(selectedImageUri);
                    }
                });

        btnChangeProfileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        btnChangeNickname.setOnClickListener(v -> showChangeNicknameDialog());

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finish();
        });

        bottomNavigation.setSelectedItemId(R.id.menu_settings);
        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_main:
                    startActivity(new Intent(this, MainActivity.class));
                    return true;
                case R.id.menu_chat:
                    startActivity(new Intent(this, RoomListActivity.class));
                    return true;
                case R.id.menu_settings:
                    return true;
            }
            return false;
        });
    }

    private void loadUserProfile() {
        userRef.get().addOnSuccessListener(snapshot -> {
            String nickname = snapshot.child("nickname").getValue(String.class);
            String profileUrl = snapshot.child("profileUrl").getValue(String.class);
            if (profileUrl != null && !profileUrl.isEmpty()) {
                Glide.with(this).load(profileUrl).into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }


            nicknameText.setText(nickname);
            if (profileUrl != null) {
                Glide.with(this)
                        .load(profileUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(profileImage);
            }
        });
    }

    private void showChangeNicknameDialog() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("닉네임 변경")
                .setMessage("새 닉네임을 입력하세요:")
                .setView(input)
                .setPositiveButton("변경", (dialog, which) -> {
                    String newNickname = input.getText().toString().trim();
                    if (!newNickname.isEmpty()) {
                        userRef.child("nickname").setValue(newNickname)
                                .addOnSuccessListener(aVoid -> {
                                    nicknameText.setText(newNickname);
                                    Toast.makeText(this, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void uploadProfileImage(Uri uri) {
        if (uri == null) return;

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profileImages")
                .child(user.getUid()).child(UUID.randomUUID().toString());

        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            userRef.child("profileUrl").setValue(downloadUri.toString());
                            Glide.with(this).load(downloadUri).into(profileImage);
                            Toast.makeText(this, "프로필 이미지가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> Toast.makeText(this, "업로드 실패", Toast.LENGTH_SHORT).show());
    }

    private void showChangePasswordDialog() {
        final EditText input = new EditText(this);
        input.setHint("새 비밀번호 (6자 이상)");
        new AlertDialog.Builder(this)
                .setTitle("비밀번호 변경")
                .setMessage("새 비밀번호를 입력하세요:")
                .setView(input)
                .setPositiveButton("변경", (dialog, which) -> {
                    String newPassword = input.getText().toString().trim();
                    if (newPassword.length() >= 6) {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "비밀번호 변경 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
