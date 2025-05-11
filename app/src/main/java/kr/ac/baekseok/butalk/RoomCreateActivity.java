package kr.ac.baekseok.butalk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RoomCreateActivity extends AppCompatActivity {

    private EditText edtRoomId, edtPassword;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_create);

        edtRoomId = findViewById(R.id.edtRoomId);
        edtPassword = findViewById(R.id.edtPassword);
        btnCreate = findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> {
            String roomId = edtRoomId.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (roomId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "ID와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);

            roomRef.child("password").get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    Toast.makeText(this, "이미 존재하는 방입니다", Toast.LENGTH_SHORT).show();
                } else {
                    // 방 정보 저장
                    roomRef.child("password").setValue(password);
                    roomRef.child("createdAt").setValue(System.currentTimeMillis());

                    // 방장 등록 및 joinAt 기록
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    long now = System.currentTimeMillis();

                    roomRef.child("owner").setValue(uid); // 방장 지정
                    roomRef.child("members").child(uid).child("joinAt").setValue(now);

                    // MainActivity로 이동
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("roomId", roomId);
                    startActivity(intent);
                    finish();
                }
            });
        });
    }
}
