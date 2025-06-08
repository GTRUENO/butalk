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

public class RoomEnterActivity extends AppCompatActivity {

    private EditText edtRoomId, edtPassword;
    private Button btnEnter;
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_enter);

        edtRoomId = findViewById(R.id.edtRoomId);
        edtPassword = findViewById(R.id.edtPassword);
        btnEnter = findViewById(R.id.btnEnter);
        db = FirebaseDatabase.getInstance();

        String prefillRoomId = getIntent().getStringExtra("roomId");
        if (prefillRoomId != null) {
            edtRoomId.setText(prefillRoomId);
        }

        btnEnter.setOnClickListener(v -> {
            String inputRoomId = edtRoomId.getText().toString().trim();
            String inputPassword = edtPassword.getText().toString().trim();

            if (inputRoomId.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "방 ID와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference roomRef = db.getReference("rooms").child(inputRoomId);
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            roomRef.child("members").child(uid).child("joinAt").get().addOnSuccessListener(memberSnapshot -> {
                if (memberSnapshot.exists()) {
                    // 이미 참여 중인 경우
                    Toast.makeText(this, "기존 참여 방입니다. 바로 입장합니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("roomId", inputRoomId);
                    startActivity(intent);
                    finish();
                } else {
                    // 새로 입장하는 경우, 비밀번호 검증
                    roomRef.child("password").get().addOnSuccessListener(pwSnapshot -> {
                        String correctPw = pwSnapshot.getValue(String.class);
                        if (correctPw != null && correctPw.equals(inputPassword)) {
                            long now = System.currentTimeMillis();
                            roomRef.child("members").child(uid).child("joinAt").setValue(now).addOnSuccessListener(task -> {
                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(uid)
                                        .child("nickname")
                                        .get()
                                        .addOnSuccessListener(nickSnap -> {
                                            String nickname = nickSnap.getValue(String.class);
                                            if (nickname != null) {
                                                roomRef.child("messages").push()
                                                        .setValue(new Message("[시스템]", nickname + "님이 입장했습니다", now));
                                            }
                                        });
                                Toast.makeText(this, "새로 입장했습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.putExtra("roomId", inputRoomId);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            Toast.makeText(this, "비밀번호가 틀리거나 방이 존재하지 않습니다", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }
}