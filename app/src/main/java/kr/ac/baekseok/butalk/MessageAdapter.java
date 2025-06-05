package kr.ac.baekseok.butalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

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
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
        } else {
            holder.layoutRight.setVisibility(View.GONE);
            holder.layoutSystem.setVisibility(View.GONE);
            holder.layoutLeft.setVisibility(View.VISIBLE);
            holder.txtNameLeft.setText(message.getSender());
            holder.txtMessageLeft.setText(message.getMessage());
            holder.txtTimeLeft.setText(formatTime(message.getTimestamp()));
            // TODO: 프로필 이미지 로딩 (예: Glide.with(...))
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
        TextView txtMessageRight, txtTimeRight;
        TextView txtSystem;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutLeft = itemView.findViewById(R.id.layoutLeft);
            layoutRight = itemView.findViewById(R.id.layoutRight);
            layoutSystem = itemView.findViewById(R.id.layoutSystem);
            txtNameLeft = itemView.findViewById(R.id.txtSenderLeft);
            imgProfile = itemView.findViewById(R.id.imgProfileLeft);
            txtMessageLeft = itemView.findViewById(R.id.txtMessageLeft);
            txtTimeLeft = itemView.findViewById(R.id.txtTimeLeft);
            txtMessageRight = itemView.findViewById(R.id.txtMessageRight);
            txtTimeRight = itemView.findViewById(R.id.txtTimeRight);
            txtSystem = itemView.findViewById(R.id.txtSystem);
        }
    }
}
