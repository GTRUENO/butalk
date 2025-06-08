package kr.ac.baekseok.butalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private ArrayList<Message> messageList;

    public MessageAdapter(ArrayList<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (currentUser != null) ? currentUser.getUid() : "";

        if ("[시스템]".equals(message.getSender())) {
            holder.layoutLeft.setVisibility(View.GONE);
            holder.layoutRight.setVisibility(View.GONE);
            holder.layoutSystem.setVisibility(View.VISIBLE);
            holder.txtSystem.setText(message.getMessage());
        } else if (message.getSender().equals(uid)) {
            holder.layoutLeft.setVisibility(View.GONE);
            holder.layoutSystem.setVisibility(View.GONE);
            holder.layoutRight.setVisibility(View.VISIBLE);
            holder.txtMessageRight.setText(message.getMessage());
            holder.txtTimeRight.setText(formatTime(message.getTimestamp()));

            // ➕ 내 닉네임 설정 추가
            FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .child("nickname")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String nickname = snapshot.getValue(String.class);
                            holder.txtSenderRight.setText(nickname != null ? nickname : "나");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            holder.txtSenderRight.setText("나");
                        }
                    });
        } else {
            holder.layoutRight.setVisibility(View.GONE);
            holder.layoutSystem.setVisibility(View.GONE);
            holder.layoutLeft.setVisibility(View.VISIBLE);

            String senderUid = message.getSender();

            // UID인지 확인
            boolean looksLikeUid = senderUid != null && senderUid.length() >= 28 && senderUid.matches("[a-zA-Z0-9]+");

            if (looksLikeUid) {
                // UID일 경우: 닉네임 가져오기
                FirebaseDatabase.getInstance().getReference("users").child(senderUid)
                        .child("nickname")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String nickname = snapshot.getValue(String.class);
                                holder.txtNameLeft.setText(nickname != null ? nickname : "알 수 없음");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                holder.txtNameLeft.setText("알 수 없음");
                            }
                        });
            } else {
                // 그냥 닉네임일 경우
                holder.txtNameLeft.setText(senderUid);
            }

            holder.txtMessageLeft.setText(message.getMessage());
            holder.txtTimeLeft.setText(formatTime(message.getTimestamp()));

            // 프로필 이미지 설정
            FirebaseDatabase.getInstance().getReference("users").child(senderUid)
                    .child("profileImageUrl")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String url = snapshot.getValue(String.class);
                            if (url != null && !url.isEmpty()) {
                                Glide.with(holder.imgProfile.getContext())
                                        .load(url)
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                        .into(holder.imgProfile);
                            } else {
                                holder.imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            holder.imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    });
        }
    }


    private String formatTime(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat;

        long now = System.currentTimeMillis();
        long todayStart = now - (now % (24 * 60 * 60 * 1000));
        long yesterdayStart = todayStart - (24 * 60 * 60 * 1000);

        if (timestamp >= todayStart) {
            dateFormat = new SimpleDateFormat("a h:mm", Locale.KOREA);
        } else if (timestamp >= yesterdayStart) {
            dateFormat = new SimpleDateFormat("어제 a h:mm", Locale.KOREA);
        } else {
            dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 a h:mm", Locale.KOREA);
        }
        return dateFormat.format(date);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutLeft, layoutRight, layoutSystem;
        ImageView imgProfile;
        TextView txtNameLeft, txtMessageLeft, txtTimeLeft;
        TextView txtSenderRight, txtMessageRight, txtTimeRight;
        TextView txtSystem;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutLeft = itemView.findViewById(R.id.layoutLeft);
            layoutRight = itemView.findViewById(R.id.layoutRight);
            layoutSystem = itemView.findViewById(R.id.layoutSystem);

            txtNameLeft = itemView.findViewById(R.id.txtSenderLeft);
            txtSenderRight = itemView.findViewById(R.id.txtSenderRight); // ⬅️ 이 부분 추가
            imgProfile = itemView.findViewById(R.id.imgProfileLeft);

            txtMessageLeft = itemView.findViewById(R.id.txtMessageLeft);
            txtTimeLeft = itemView.findViewById(R.id.txtTimeLeft);

            txtMessageRight = itemView.findViewById(R.id.txtMessageRight);
            txtTimeRight = itemView.findViewById(R.id.txtTimeRight);

            txtSystem = itemView.findViewById(R.id.txtSystem);
        }
    }

}
