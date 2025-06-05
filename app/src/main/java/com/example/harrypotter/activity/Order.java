package com.example.harrypotter.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.harrypotter.HTTPHelper;
import com.example.harrypotter.R;
import com.example.harrypotter.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Order extends BaseActivity {
    protected ArrayList<Product> products = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.order);

        onBottomButtonClick(R.id.products_button, MainActivity.class);
        onBottomButtonClick(R.id.favorite_button, Favorite.class);
        onBottomButtonClick(R.id.bin_button, Bin.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new GetBinProductsTask().execute(HTTPHelper.baseUrl + "/bin/" + getIntent().getIntExtra("user_id", 0));
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
                    products.add(HTTPHelper.createProduct(jsonObject));
                }
                createProductCards(R.id.order_cards, products);
            } catch (JSONException e) { showErrorMessage(Order.this); }
        }
    }

    protected void createProductCards(int id, ArrayList<Product> products) {
        try {
            LinearLayout productsLayout = findViewById(id);
            productsLayout.removeAllViews();
            int sum = 0;
            for (int i = 0; i < products.size(); i++) {
                View card = createOrderCard(products.get(i));
                productsLayout.addView(card);
                sum += products.get(i).price;
            }

            TextView tolalPrice = findViewById(R.id.itog);
            tolalPrice.setText("К оплате: " + sum + " р.");
        }
        catch (Exception ex) {
            showErrorMessage(this);
        }
    }

    protected View createOrderCard(Product product) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View card = inflater.inflate(R.layout.order_card, null);

        TextView cardTitle = card.findViewById(R.id.title);
        cardTitle.setText(product.title);

        TextView cardPrice = card.findViewById(R.id.price);
        cardPrice.setText(String.valueOf(product.price));

        ImageView imageView = card.findViewById(R.id.image);
        Glide.with(this).load(product.photoPath + "?raw=true").into(imageView);

        return card;
    }
}