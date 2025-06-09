package kr.ac.baekseok.butalk;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class MenuActivity extends AppCompatActivity {

    private TextView nicknameGreeting;
    private Button btnSmartAttendance, btnEverytime;
    private Button btnHomepage, btnInfoSystem, btnCyberCampus;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        nicknameGreeting = findViewById(R.id.nicknameGreeting);
        btnSmartAttendance = findViewById(R.id.btnSmartAttendance);
        btnEverytime = findViewById(R.id.btnEverytime);
        btnHomepage = findViewById(R.id.btnHomepage);
        btnInfoSystem = findViewById(R.id.btnInfoSystem);
        btnCyberCampus = findViewById(R.id.btnCyberCampus);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Firebase에서 닉네임 불러오기
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.child("nickname").get().addOnSuccessListener(snapshot -> {
            String nickname = snapshot.getValue(String.class);
            nicknameGreeting.setText(nickname + "님\n오늘도 화이팅 하세요 :)");
        });

        // 스마트 출결
        btnSmartAttendance.setOnClickListener(v -> openAppOrMarket("com.libeka.attendance.ucheckplusstud_baekseok"));

        // 에브리타임
        btnEverytime.setOnClickListener(v -> openAppOrMarket("com.everytime.v2"));

        // 웹 링크들
        btnHomepage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bu.ac.kr/web/index.do"));
            startActivity(intent);
        });

        btnInfoSystem.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tcms.bu.ac.kr/index.do"));
            startActivity(intent);
        });
        btnCyberCampus.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bctl.bu.ac.kr/home/mainHome/Form/main"));
            startActivity(intent);
        });

        // 하단 네비게이션
        bottomNavigation.setSelectedItemId(R.id.menu_main);
        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_main:
                    return true;
                case R.id.menu_chat:
                    startActivity(new Intent(this, RoomListActivity.class));
                    return true;
                case R.id.menu_settings:
                    startActivity(new Intent(this, SettingsActivity.class));
                    return true;
            }
            return false;
        });
    }

    private void openAppOrMarket(String packageName) {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            startActivity(launchIntent); // 앱 실행
        } else {
            // 플레이스토어로 이동
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            startActivity(marketIntent);
        }
    }
}
