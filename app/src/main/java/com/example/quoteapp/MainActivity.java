package com.example.quoteapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextView tvQuote, tvAuthor;
    Button btnNewQuote, btnShare;
    String currentQuote = "";

    ArrayList<String[]> localQuotes;
    Random random = new Random();
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvQuote = findViewById(R.id.tvQuote);
        tvAuthor = findViewById(R.id.tvAuthor);
        btnNewQuote = findViewById(R.id.btnNewQuote);
        btnShare = findViewById(R.id.btnShare);

        queue = Volley.newRequestQueue(this);
        setupLocalQuotes();

        // First Load
        if (isNetworkConnected()) {
            fetchQuoteFromAPI();
        } else {
            showOfflineQuote();
        }

        // Button to Fetch New Quote
        btnNewQuote.setOnClickListener(v -> {
            if (isNetworkConnected()) {
                fetchQuoteFromAPI();
            } else {
                showOfflineQuote();
            }
        });

        // Button to Share
        btnShare.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, currentQuote);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share via"));
        });
    }

    // ✅ Fetch from API
    private void fetchQuoteFromAPI() {
        String url = "https://api.quotable.io/random";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String quote = response.getString("content");
                        String author = response.getString("author");
                        updateQuoteUI(quote, author);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showOfflineQuote();
                    }
                },
                error -> {
                    error.printStackTrace();
                    showOfflineQuote();
                });

        request.setShouldCache(false); // 🔥 Force fresh quote every time
        queue.add(request);
    }

    // ✅ Update UI with animation
    private void updateQuoteUI(String quote, String author) {
        tvQuote.setAlpha(0f);
        tvAuthor.setAlpha(0f);

        tvQuote.setText('"' + quote + '"');
        tvAuthor.setText("- " + author);

        tvQuote.animate().alpha(1f).setDuration(500).start();
        tvAuthor.animate().alpha(1f).setDuration(500).start();

        currentQuote = '"' + quote + "\"\n- " + author;
    }

    // ✅ Offline fallback quotes
    private void setupLocalQuotes() {
        localQuotes = new ArrayList<>();
        localQuotes.add(new String[]{"The best way to predict the future is to invent it.", "Alan Kay"});
        localQuotes.add(new String[]{"Be yourself; everyone else is already taken.", "Oscar Wilde"});
        localQuotes.add(new String[]{"Success is not final, failure is not fatal.", "Winston Churchill"});
        localQuotes.add(new String[]{"In the middle of every difficulty lies opportunity.", "Albert Einstein"});
        localQuotes.add(new String[]{"Dream big and dare to fail.", "Norman Vaughan"});
    }

    private void showOfflineQuote() {
        int index = random.nextInt(localQuotes.size());
        String quote = localQuotes.get(index)[0];
        String author = localQuotes.get(index)[1];
        updateQuoteUI(quote, author);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}
