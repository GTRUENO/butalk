package kr.ac.baekseok.butalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editMessage;
    private Button btnSend, btnExitOrDelete;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private ArrayList<Message> messageList = new ArrayList<>();

    private DatabaseReference chatRef;
    private String roomId;
    private boolean isOwner = false;
    private long joinAt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null || roomId.isEmpty()) roomId = "defaultRoom";

        chatRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId).child("messages");

        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnExitOrDelete = findViewById(R.id.btnExitOrDelete);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 🔸 방장 여부 판단
        FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId)
                .child("owner")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && uid.equals(snapshot.getValue(String.class))) {
                        isOwner = true;
                        btnExitOrDelete.setText("방 삭제하기");
                    } else {
                        btnExitOrDelete.setText("방 나가기");
                    }
                });

        btnExitOrDelete.setOnClickListener(v -> {
            if (isOwner) {
                postSystemMessage("방장이 방을 삭제했습니다");
                FirebaseDatabase.getInstance().getReference("rooms")
                        .child(roomId)
                        .removeValue()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "방을 삭제했습니다", Toast.LENGTH_SHORT).show();
                            finish();
                        });
            } else {
                FirebaseDatabase.getInstance().getReference("users")
                        .child(uid)
                        .child("nickname")
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            String nickname = snapshot.getValue(String.class);
                            if (nickname != null) {
                                postSystemMessage(nickname + "님이 나갔습니다");
                            }
                        });

                FirebaseDatabase.getInstance().getReference("rooms")
                        .child(roomId)
                        .child("members")
                        .child(uid)
                        .removeValue()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "방에서 나갔습니다", Toast.LENGTH_SHORT).show();
                            finish();
                        });
            }
        });

        // 입장 메시지 출력
//        FirebaseDatabase.getInstance().getReference("users")
//                .child(uid)
//                .child("nickname")
//                .get()
//                .addOnSuccessListener(snapshot -> {
//                    String nickname = snapshot.getValue(String.class);
//                    if (nickname != null) {
//                        postSystemMessage(nickname + "님이 입장했습니다");
//                    }
//                });

        btnSend.setOnClickListener(v -> sendMessage());

        receiveMessages();
    }

    private void sendMessage() {
        String msg = editMessage.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .child("nickname")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String nickname = snapshot.getValue(String.class);
                    if (nickname == null) nickname = "알 수 없음";

                    long timestamp = System.currentTimeMillis();
                    Message message = new Message(nickname, msg, timestamp);

                    String messageId = chatRef.push().getKey();
                    chatRef.child(messageId).setValue(message);

                    editMessage.setText("");
                });
    }

    private void postSystemMessage(String text) {
        long timestamp = System.currentTimeMillis();
        Message message = new Message("[시스템]", text, timestamp);
        String messageId = chatRef.push().getKey();
        chatRef.child(messageId).setValue(message);
    }

    private void receiveMessages() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId)
                .child("members")
                .child(uid)
                .child("joinAt")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        joinAt = snapshot.getValue(Long.class);

                        chatRef.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                                Message message = snapshot.getValue(Message.class);
                                if (message != null && message.getTimestamp() >= joinAt) {
                                    messageList.add(message);
                                    adapter.notifyItemInserted(messageList.size() - 1);
                                    recyclerView.scrollToPosition(messageList.size() - 1);
                                }
                            }

                            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                });
    }
}