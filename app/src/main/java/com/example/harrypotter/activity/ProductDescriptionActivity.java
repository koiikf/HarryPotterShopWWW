package com.example.harrypotter.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.harrypotter.HTTPHelper;
import com.example.harrypotter.R;
import com.example.harrypotter.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProductDescriptionActivity extends BaseActivity {
    boolean isFavorite = false;
    boolean isBin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.product_description);

        onBottomButtonClick(R.id.products_button, MainActivity.class);
        onBottomButtonClick(R.id.favorite_button, Favorite.class);
        onBottomButtonClick(R.id.bin_button, Bin.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button favoriteButton = findViewById(R.id.add_favorite_button);
        favoriteButton.setOnClickListener(view -> {
            addFavourity(getIntent().getIntExtra("product_id", 0));
        });

        Button binButton = findViewById(R.id.add_bin_button);
        binButton.setOnClickListener(view -> {
            addBin(getIntent().getIntExtra("product_id", 0));
        });

        try {
            new GetFavourityProductsTask().execute(HTTPHelper.baseUrl + "/favorite/" + getIntent().getIntExtra("user_id", 0));
            new GetBinProductsTask().execute(HTTPHelper.baseUrl + "/bin/" + getIntent().getIntExtra("user_id", 0));
            new GetProductTask(R.id.product_title, R.id.product_price, R.id.product_description, R.id.img)
                    .execute(HTTPHelper.baseUrl + "/products/" + getIntent().getIntExtra("product_id", 0));
        }
        catch (Exception ex) {}
    }

    public class GetFavourityProductsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return HTTPHelper.createConnectionAndReadData(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getInt("id") == getIntent().getIntExtra("product_id", 0)) {
                        Button favoriteButton = findViewById(R.id.add_favorite_button);
                        isFavorite = true;
                        favoriteButton.setText("❤\uFE0F");
                    }
                }
                // После отображения меняем их цвет в соответствии с избранным
            } catch (JSONException e) { }
        }
    }

    public class GetBinProductsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return HTTPHelper.createConnectionAndReadData(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getInt("id") == getIntent().getIntExtra("product_id", 0)) {
                        Button binButton = findViewById(R.id.add_bin_button);
                        isBin = true;
                        binButton.setText("\uD83D\uDED2");
                    }
                }
                // После отображения меняем их цвет в соответствии с избранным
            } catch (JSONException e) { }
        }
    }

    protected void addFavourity(int id) {
        JSONObject object = new JSONObject();
        try {
            if (!isFavorite) {
                object.put("user_id", getIntent().getIntExtra("user_id", 0));
                object.put("product_id", id);
                new HTTPHelper.PostJsonRequestTask(object).execute(HTTPHelper.baseUrl + "/favorite");


                Button favoriteButton = findViewById(R.id.add_favorite_button);
                favoriteButton.setText("❤\uFE0F");
                isFavorite = true;
            } else {
                try {
                    new HTTPHelper.DeleteJsonRequestTask(new JSONObject()).execute(HTTPHelper.baseUrl + "/favorite/" + getIntent().getIntExtra("user_id", 0) + "/" + getIntent().getIntExtra("product_id", 0));
                    Button favoriteButton = findViewById(R.id.add_favorite_button);
                    favoriteButton.setText("\uD83E\uDD0D");
                    isFavorite = false;
                }
                catch (Exception ex) { showErrorMessage(this);}
            }
        } catch (JSONException e) {  }
    }

    protected void addBin(int id) {
        JSONObject object = new JSONObject();
        try {
            if (!isBin) {
                object.put("user_id", getIntent().getIntExtra("user_id", 0));
                object.put("product_id", id);
                new HTTPHelper.PostJsonRequestTask(object).execute(HTTPHelper.baseUrl + "/bin");


                Button binButton = findViewById(R.id.add_bin_button);
                binButton.setText("\uD83D\uDED2");
                isBin = true;
            } else {
                try {
                    new HTTPHelper.DeleteJsonRequestTask(new JSONObject()).execute(HTTPHelper.baseUrl + "/bin/" + getIntent().getIntExtra("user_id", 0) + "/" + getIntent().getIntExtra("product_id", 0));
                    Button binButton = findViewById(R.id.add_bin_button);
                    binButton.setText("\uD83E\uDDFA");
                    isBin = false;
                }
                catch (Exception ex) { showErrorMessage(this);}
            }
        } catch (JSONException e) {  }
    }
}