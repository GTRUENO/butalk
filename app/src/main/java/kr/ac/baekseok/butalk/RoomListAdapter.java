package kr.ac.baekseok.butalk;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.RoomViewHolder> {

    private ArrayList<RoomInfo> roomList;
    private Context context;

    public RoomListAdapter(ArrayList<RoomInfo> roomList, Context context) {
        this.roomList = roomList;
        this.context = context;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomInfo info = roomList.get(position);

        holder.textRoomId.setText(info.getRoomId());
        holder.textLastMessage.setText(info.getSender() + ": " + info.getLastMessage());
        holder.textMemberCount.setText("현재 인원: " + info.getMemberCount() + "명");

        // 시간 포맷 변환
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(new Date(info.getTimestamp()));
        holder.textTimestamp.setText(formattedTime);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("roomId", info.getRoomId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView textRoomId, textLastMessage, textMemberCount, textTimestamp;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            textRoomId = itemView.findViewById(R.id.textRoomId);
            textLastMessage = itemView.findViewById(R.id.textLastMessage);
            textMemberCount = itemView.findViewById(R.id.textMemberCount);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
        }
    }
}
