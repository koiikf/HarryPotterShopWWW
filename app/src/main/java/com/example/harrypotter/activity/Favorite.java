package com.example.harrypotter.activity;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
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

public class Favorite extends BaseActivity {
    protected ArrayList<Product> products = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.favorite);
        onBottomButtonClick(R.id.products_button, MainActivity.class);
        onBottomButtonClick(R.id.favorite_button, Favorite.class);
        onBottomButtonClick(R.id.bin_button, Bin.class);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new GetFavourityProductsTask().execute(HTTPHelper.baseUrl + "/favorite/" + getIntent().getIntExtra("user_id", 0));
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
                    products.add(HTTPHelper.createProduct(jsonObject));
                }
                createProductCards(R.id.products, products);
                // После отображения меняем их цвет в соответствии с избранным
            } catch (JSONException e) { showErrorMessage(Favorite.this); }
        }
    }

    protected void createProductCards(int id, ArrayList<Product> products) {
        LinearLayout productsLayout = findViewById(id);
        productsLayout.removeAllViews();

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        int cardsPerRow = screenWidth < 720 ? 1 : 2;

        TableRow row = null;

        for (int i = 0; i < products.size(); i++) {
            if (i % cardsPerRow == 0) {
                row = new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));
                productsLayout.addView(row);
            }

            View card = createProductCard(products.get(i));
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            card.setLayoutParams(params);
            row.addView(card);
        }
    }
}