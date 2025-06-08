package kr.ac.baekseok.butalk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class RoomListActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> roomIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_room_list);

        listView = findViewById(R.id.listViewRooms);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roomIds);
        listView.setAdapter(adapter);

        // 현재 로그인된 사용자 UID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 내가 속해 있는 방만 표시
        FirebaseDatabase.getInstance().getReference("rooms")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        roomIds.clear();
                        for (DataSnapshot room : snapshot.getChildren()) {
                            // 🔽 members 안에 내 UID가 있으면 해당 방을 리스트에 추가
                            if (room.child("members").hasChild(uid)) {
                                roomIds.add(room.getKey());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RoomListActivity.this, "불러오기 실패", Toast.LENGTH_SHORT).show();
                    }
                });

        // 클릭 시 RoomEnterActivity로 이동
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRoom = roomIds.get(position);
            DatabaseReference memberRef = FirebaseDatabase.getInstance()
                    .getReference("rooms")
                    .child(selectedRoom)
                    .child("members")
                    .child(uid)
                    .child("joinAt");

            memberRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    // 이미 참가한 방이므로 → 바로 입장
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("roomId", selectedRoom);
                    startActivity(intent);
                } else {
                    // 처음 들어가는 방이므로 → 비밀번호 입력 화면으로 이동
                    Intent intent = new Intent(this, RoomEnterActivity.class);
                    intent.putExtra("roomId", selectedRoom);  // 미리 전달
                    startActivity(intent);
                }
            });
        });
    }
}
