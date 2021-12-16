package com.specikman.demobaitap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.specikman.demobaitap.databinding.ActivityMainBinding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding = null;
    private final String CHANNEL_ID = "ChannelID";
    private final String CHANNEL_NAME = "ChannelName";
    private final Integer NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        createNotificationChannel();
        binding.btnShow.setEnabled(false);
        binding.btnGetQuestion.setOnClickListener(e -> {
            getAndStoreQuestionsFromApi();
        });

        binding.btnShow.setOnClickListener(e -> {
            List<Question> questions = convertToListQuestions(
                    getSharedPreferences("myPref", Context.MODE_PRIVATE)
                            .getString("response", "No value"));
            Log.d("API", questions.size() + "");
            Question randQuestion = questions.get(new Random().nextInt(questions.size()));
            showNotification(randQuestion);
        });
    }

    public void getAndStoreQuestionsFromApi() {
        //Init
        String API_URL = "https://opentdb.com/api.php?amount=10"; //API URL
        //Request API
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,
                response -> {
                    String response_string = response.toString();
                    SharedPreferences sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);
                    sharedPref.edit().putString("response", response_string).apply();
                    Toast.makeText(this, "Questions stored in pref", Toast.LENGTH_SHORT).show();
                    binding.btnShow.setEnabled(true);
                },
                error -> Toast.makeText(this, "Error: " + error.toString(), Toast.LENGTH_LONG)
        );
        queue.add(request);
    }

    public List<Question> convertToListQuestions(String response_string) {
        JsonObject jsonObj = new Gson().fromJson(response_string, JsonElement.class).getAsJsonObject();
        Type listType = new TypeToken<List<Question>>() {
        }.getType();
        JsonElement element = jsonObj.get("results");
        return new Gson().fromJson(element, listType);
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(Question q) {
        //init
        List<String> shuffle = q.getIncorrect_answers(); //Notification default chỉ cho phép 3 button tức 3 câu trả lời
        String str = shuffle.remove(shuffle.size() - 1); //Bỏ 1 câu trả lời sai vì chỉ show 3 câu trả lời. API trả về tổng cộng 4 câu
        shuffle.add(q.getCorrect_answer());//add câu trả lời đúng vào chung câu trả lời sai để shuffle
        Collections.shuffle(shuffle);//Shuffle thứ tự câu trả lời
        //-----------------------------------------------------------------

        String contentShow = q.getQuestion().replace("&quot;", "\"") + "\n1). " +
                shuffle.get(0) + "\n2). " +
                shuffle.get(1) + "\n3). " +
                shuffle.get(2);

        //Action 1-----------------------------------------------------------------
        Intent broadcastIntent = new Intent(this, BroadcastReceiver1.class);
        if (shuffle.get(0).equals(q.getCorrect_answer())) {
            broadcastIntent.putExtra("choose", shuffle.get(0));
            broadcastIntent.putExtra("answer", q.getCorrect_answer());
            broadcastIntent.putExtra("notification_id", NOTIFICATION_ID);
        } else {
            broadcastIntent.putExtra("choose", "Not choose");
            broadcastIntent.putExtra("answer", "Not answer");
        }
        PendingIntent actionIntent1 = PendingIntent.getBroadcast(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Action 2-----------------------------------------------------------------
        Intent broadcastIntent2 = new Intent(this, BroadcastReceiver2.class);
        if (shuffle.get(1).equals(q.getCorrect_answer())) {
            broadcastIntent2.putExtra("choose", shuffle.get(1));
            broadcastIntent2.putExtra("answer", q.getCorrect_answer());
            broadcastIntent2.putExtra("notification_id", NOTIFICATION_ID);
        }else {
            broadcastIntent2.putExtra("choose", "Not choose");
            broadcastIntent2.putExtra("answer", "Not answer");
        }
        PendingIntent actionIntent2 = PendingIntent.getBroadcast(this, 0, broadcastIntent2, PendingIntent.FLAG_UPDATE_CURRENT);

        //Action 3-----------------------------------------------------------------
        Intent broadcastIntent3 = new Intent(this, BroadcastReceiver3.class);
        if (shuffle.get(2).equals(q.getCorrect_answer())) {
            broadcastIntent3.putExtra("choose", shuffle.get(2));
            broadcastIntent3.putExtra("answer", q.getCorrect_answer());
            broadcastIntent3.putExtra("notification_id", NOTIFICATION_ID);
        }else {
            broadcastIntent3.putExtra("choose", "Not choose");
            broadcastIntent3.putExtra("answer", "Not answer");
        }

        PendingIntent actionIntent3 = PendingIntent.getBroadcast(this, 0, broadcastIntent3, PendingIntent.FLAG_UPDATE_CURRENT);
        //Create notification-----------------------
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Category - " + q.getCategory())
                .setContentText(q.getQuestion())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentShow))
                .setColor(Color.BLUE)
                .addAction(R.mipmap.ic_launcher, "Answer 1", actionIntent1)
                .addAction(R.mipmap.ic_launcher, "Answer 2", actionIntent2)
                .addAction(R.mipmap.ic_launcher, "Answer 3", actionIntent3)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(10, 0, false);
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(NOTIFICATION_ID, builder.build());

    }

}