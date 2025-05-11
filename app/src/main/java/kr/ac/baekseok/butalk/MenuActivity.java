package kr.ac.baekseok.butalk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private Button btnEnterRoom, btnRoomList, btnMakeRoom, btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnEnterRoom = findViewById(R.id.btnEnterRoom);
        btnRoomList = findViewById(R.id.btnRoomList);
        btnMakeRoom = findViewById(R.id.btnMakeRoom);
        btnSettings = findViewById(R.id.btnSettings);


        btnEnterRoom.setOnClickListener(v -> {
            startActivity(new Intent(this, RoomEnterActivity.class));
        });

        btnRoomList.setOnClickListener(v -> {
            startActivity(new Intent(this, RoomListActivity.class));
        });

        btnMakeRoom.setOnClickListener(v -> {
            startActivity(new Intent(this, RoomCreateActivity.class));
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

    }
}
