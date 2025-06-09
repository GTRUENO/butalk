package kr.ac.baekseok.butalk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class RoomListActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView nicknameText;
    private Button btnCreateRoom, btnJoinRoom;
    private RecyclerView roomRecyclerView;
    private BottomNavigationView bottomNavigation;

    private RoomListAdapter adapter;
    private ArrayList<RoomInfo> roomList = new ArrayList<>();
    private HashMap<String, String> nicknameCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_room_list);

        profileImage = findViewById(R.id.profileImage);
        nicknameText = findViewById(R.id.nicknameText);
        btnCreateRoom = findViewById(R.id.btnCreateRoom);
        btnJoinRoom = findViewById(R.id.btnJoinRoom);
        roomRecyclerView = findViewById(R.id.roomRecyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        roomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomListAdapter(roomList, this);
        roomRecyclerView.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 사용자 정보 불러오기
        FirebaseDatabase.getInstance().getReference("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nickname = snapshot.child("nickname").getValue(String.class);
                        String profileUrl = snapshot.child("profileUrl").getValue(String.class);
                        nicknameText.setText(nickname);
                        Glide.with(RoomListActivity.this).load(profileUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(profileImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        btnCreateRoom.setOnClickListener(v -> startActivity(new Intent(this, RoomCreateActivity.class)));
        btnJoinRoom.setOnClickListener(v -> startActivity(new Intent(this, RoomEnterActivity.class)));

        // 실시간 채팅방 목록 반영
        FirebaseDatabase.getInstance().getReference("rooms")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        roomList.clear();
                        for (DataSnapshot room : snapshot.getChildren()) {
                            if (room.child("members").hasChild(uid)) {
                                String roomId = room.getKey();
                                String lastMsg = "";
                                String senderUid = "";
                                long timestamp = 0;
                                int memberCount = (int) room.child("members").getChildrenCount();

                                for (DataSnapshot msgSnap : room.child("messages").getChildren()) {
                                    Message m = msgSnap.getValue(Message.class);
                                    if (m != null && m.getTimestamp() > timestamp) {
                                        timestamp = m.getTimestamp();
                                        lastMsg = m.getMessage();
                                        senderUid = m.getSender();
                                    }
                                }

                                long finalTimestamp = timestamp;
                                String finalLastMsg = lastMsg;
                                String finalSenderUid = senderUid;

                                if (finalSenderUid.startsWith("[")) {
                                    RoomInfo info = new RoomInfo(roomId, finalLastMsg, finalSenderUid, memberCount, finalTimestamp);
                                    roomList.add(info);
                                    sortAndRefresh();
                                    continue;
                                }

                                if (nicknameCache.containsKey(finalSenderUid)) {
                                    RoomInfo info = new RoomInfo(roomId, finalLastMsg, nicknameCache.get(finalSenderUid), memberCount, finalTimestamp);
                                    roomList.add(info);
                                    sortAndRefresh();
                                } else {
                                    FirebaseDatabase.getInstance().getReference("users")
                                            .child(finalSenderUid)
                                            .child("nickname")
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    String nickname = snapshot.getValue(String.class);
                                                    if (nickname == null) nickname = "알 수 없음";
                                                    nicknameCache.put(finalSenderUid, nickname);

                                                    RoomInfo info = new RoomInfo(roomId, finalLastMsg, nickname, memberCount, finalTimestamp);
                                                    roomList.add(info);
                                                    sortAndRefresh();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {}
                                            });
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RoomListActivity.this, "채팅방 목록 불러오기 실패", Toast.LENGTH_SHORT).show();
                    }
                });

        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_main:
                    startActivity(new Intent(this, MenuActivity.class));
                    return true;
                case R.id.menu_chat:
                    return true; // 현재 화면 유지
                case R.id.menu_settings:
                    startActivity(new Intent(this, SettingsActivity.class));
                    return true;
            }
            return false;
        });
    }

    private void sortAndRefresh() {
        Collections.sort(roomList, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        adapter.notifyDataSetChanged();
    }
}
