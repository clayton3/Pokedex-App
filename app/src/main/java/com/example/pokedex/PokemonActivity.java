package com.example.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView, numberTextView, type1TextView, type2TextView;
    private RequestQueue requestQueue;
    private String url;
    private Boolean caught;
    private TextView catchButton;
    private SharedPreferences sharedPreference;
    private ImageView pokeSprite;
    public static final String mypreference = "mypref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        pokeSprite = findViewById(R.id.pokemon_sprite);

        catchButton = findViewById(R.id.catchButton);
        sharedPreference = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        load();
    }

    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            pokeSprite.setImageBitmap(bitmap);
        }
    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String name = response.getString("name");
                    nameTextView.setText(name.substring(0,1).toUpperCase()+name.substring(1));
                    numberTextView.setText(String.format("#%03d",response.getInt("id")));

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1)
                            type1TextView.setText(type.substring(0,1).toUpperCase()+type.substring(1));
                        else if (slot == 2)
                            type2TextView.setText(type.substring(0,1).toUpperCase()+type.substring(1));
                    }
                    JSONObject sprite = response.getJSONObject("sprites");
                    new DownloadSpriteTask().execute(sprite.getString("front_default"));
                    if (sharedPreference.contains(nameTextView.getText().toString()))
                        catchButton.setText("Release");
                } catch (JSONException e) {
                    Log.e("pokeerror", "Pokemon Json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("pokeerror", "Pokemon details Error");
            }
        });

        requestQueue.add(request);
    }

    public void toggleCatch(View view) {
        String name = nameTextView.getText().toString();
        SharedPreferences.Editor editor = sharedPreference.edit();

        if (catchButton.getText().toString().toLowerCase().equals("Catch".toLowerCase())) {
            //editor.clear();
            editor.putString(name, name);
            editor.apply();
            editor.commit();
            catchButton.setText("Release");
        } else if (catchButton.getText().toString().toLowerCase().equals("Release".toLowerCase())){
            editor.remove(name);
            editor.apply();
            editor.commit();
            catchButton.setText("Catch");
        }
    }
}