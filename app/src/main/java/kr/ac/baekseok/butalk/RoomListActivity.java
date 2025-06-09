package kr.ac.baekseok.butalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class RoomListActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView nicknameText;
    private Button btnCreateRoom, btnJoinRoom;
    private RecyclerView roomRecyclerView;
    private BottomNavigationView bottomNavigation;

    private RoomListAdapter adapter;
    private ArrayList<RoomInfo> roomList = new ArrayList<>();
    private HashMap<String, String> nicknameCache = new HashMap<>();
    private DatabaseReference userRef;
    private StorageReference storage;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    uploadProfileImage(selectedImage);
                }
            });

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
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        storage = FirebaseStorage.getInstance().getReference("profile_images").child(uid + ".jpg");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nickname = snapshot.child("nickname").getValue(String.class);
                String profileUrl = snapshot.child("profileUrl").getValue(String.class);

                if (profileUrl != null && !profileUrl.isEmpty()) {
                    Glide.with(RoomListActivity.this).load(profileUrl).into(profileImage);
                } else {
                    profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                }

                nicknameText.setText(nickname);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 채팅방 목록 로딩
        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("rooms");

        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear();
                for (DataSnapshot room : snapshot.getChildren()) {
                    if (room.child("members").hasChild(uid)) {
                        String roomId = room.getKey();
                        String lastMsg = "";
                        String sender = "";
                        long timestamp = 0;
                        int memberCount = (int) room.child("members").getChildrenCount();

                        for (DataSnapshot msgSnap : room.child("messages").getChildren()) {
                            Message m = msgSnap.getValue(Message.class);
                            if (m != null && m.getTimestamp() > timestamp) {
                                timestamp = m.getTimestamp();
                                lastMsg = m.getMessage();
                                sender = m.getSender();
                            }
                        }

                        if (sender.startsWith("[")) {
                            roomList.add(new RoomInfo(roomId, lastMsg, sender, memberCount, timestamp));
                            continue;
                        }

                        String finalLastMsg = lastMsg;
                        String finalSender = sender;
                        long finalTimestamp = timestamp;

                        if (nicknameCache.containsKey(finalSender)) {
                            String nick = nicknameCache.get(finalSender);
                            roomList.add(new RoomInfo(roomId, finalLastMsg, nick, memberCount, finalTimestamp));
                            roomList.sort(Comparator.comparingLong(RoomInfo::getTimestamp).reversed());
                            adapter.notifyDataSetChanged();
                        } else {
                            FirebaseDatabase.getInstance().getReference("users").child(finalSender).child("nickname")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String nickname = snapshot.getValue(String.class);
                                            if (nickname == null) nickname = "알 수 없음";
                                            nicknameCache.put(finalSender, nickname);
                                            roomList.add(new RoomInfo(roomId, finalLastMsg, nickname, memberCount, finalTimestamp));
                                            adapter.notifyDataSetChanged();
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
                Toast.makeText(RoomListActivity.this, "불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });

        profileImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            } else {
                pickImage();
            }
        });

        btnCreateRoom.setOnClickListener(v -> startActivity(new Intent(this, RoomCreateActivity.class)));
        btnJoinRoom.setOnClickListener(v -> startActivity(new Intent(this, RoomEnterActivity.class)));

        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_main:
                    startActivity(new Intent(this, MenuActivity.class));
                    return true;
                case R.id.menu_chat:
                    return true;
                case R.id.menu_settings:
                    startActivity(new Intent(this, SettingsActivity.class));
                    return true;
            }
            return false;
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        storage.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storage.getDownloadUrl().addOnSuccessListener(uri -> {
                    String url = uri.toString();
                    userRef.child("profileUrl").setValue(url);
                    Glide.with(this).load(url).into(profileImage);
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
