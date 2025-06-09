package kr.ac.baekseok.butalk;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;

public class RoomEnterActivity extends AppCompatActivity {

    private EditText editRoomId, editRoomPassword;
    private Button btnJoinRoomFinal;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_enter);

        editRoomId = findViewById(R.id.editRoomId);
        editRoomPassword = findViewById(R.id.editRoomPassword);
        btnJoinRoomFinal = findViewById(R.id.btnJoinRoomFinal);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        btnJoinRoomFinal.setOnClickListener(v -> enterRoom());
    }

    private void enterRoom() {
        String roomId = editRoomId.getText().toString().trim();
        String inputPw = editRoomPassword.getText().toString().trim();
        String uid = auth.getCurrentUser().getUid();

        if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(inputPw)) {
            Toast.makeText(this, "모든 칸을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        dbRef.child("rooms").child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(RoomEnterActivity.this, "존재하지 않는 방입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String correctPw = snapshot.child("password").getValue(String.class);
                if (!inputPw.equals(correctPw)) {
                    Toast.makeText(RoomEnterActivity.this, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 멤버로 등록
                dbRef.child("rooms").child(roomId).child("members").child(uid)
                        .child("joinAt").setValue(System.currentTimeMillis());

                Intent intent = new Intent(RoomEnterActivity.this, MainActivity.class);
                intent.putExtra("roomId", roomId);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RoomEnterActivity.this, "에러: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
