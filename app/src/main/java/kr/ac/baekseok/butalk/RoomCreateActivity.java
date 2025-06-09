package kr.ac.baekseok.butalk;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RoomCreateActivity extends AppCompatActivity {

    private EditText editRoomId, editPassword;
    private Button btnCreate;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_create);

        editRoomId = findViewById(R.id.editRoomId);
        editPassword = findViewById(R.id.editRoomPassword);
        btnCreate = findViewById(R.id.btnCreateRoomFinal);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        btnCreate.setOnClickListener(v -> createRoom());
    }

    private void createRoom() {
        String roomId = editRoomId.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String uid = auth.getCurrentUser().getUid();
        long joinAt = System.currentTimeMillis();

        if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "모든 칸을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> roomData = new HashMap<>();
        roomData.put("password", password);
        roomData.put("owner", uid);

        HashMap<String, Object> memberData = new HashMap<>();
        memberData.put("joinAt", joinAt);

        roomData.put("members", new HashMap<String, Object>() {{
            put(uid, memberData);
        }});

        dbRef.child("rooms").child(roomId).setValue(roomData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "채팅방이 생성되었습니다!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
