package com.example.harrypotter.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.harrypotter.HTTPHelper;
import com.example.harrypotter.R;
import com.example.harrypotter.model.Product;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {

    protected void onBottomButtonClick(int viewId, Class<?> activity) {
        View button = findViewById(viewId);
        if (button != null) {
            button.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), activity);
                intent.putExtra("user_id", getIntent().getIntExtra("user_id", 0));
                startActivity(intent);
            });
        }
    }

    protected String encodeImage(int id) {
        Bitmap bitmap = ((BitmapDrawable) ((ImageView)(findViewById(id))).getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    protected void changeImage(int id, int image) {
        Button changeImage = findViewById(id);
        changeImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, image);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView photoImage = findViewById(requestCode);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                photoImage.setImageBitmap(bitmap);
            } catch (Exception e) {}
        }
    }

    protected void showMessage(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    protected void showErrorMessage(Context context) {
        showMessage(context, "Произошла ошибка!");
    }

    protected String setTime() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return now.format(formatter);
        }
        return "";
    }

    protected void setTextInTextView(int id, String text) {
        TextView textView = findViewById(id);
        textView.setText(text);
    }

    protected boolean onKeyPush(View v, int keyCode, KeyEvent event, EditText findText, ArrayList<Product> notes, int id) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            String searchText = findText.getText().toString();
            findByTitle(searchText, notes, id);
            return true;
        }
        return false;
    }


    protected void findByTitle(String text, ArrayList<Product> products, int id) {
        int count = 0;
        ArrayList<Product> productsNew = new ArrayList<>();
        if (!products.isEmpty()) {
            for (Product product: products) {
                if (product.title.toLowerCase().contains(text.toLowerCase()) || product.title.toLowerCase().contains(text.toLowerCase())) {
                    count += 1;
                    productsNew.add(product);
                }
            }
            createProductCards(id, productsNew);
            // Обновляем темы карточек
        }
        if (count == 0) { showMessage(this, "Товаров не найдено!"); }
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

    protected void findListener(ArrayList<Product> products, int id1, int id2) {
        EditText findText = findViewById(id1);
        findText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return onKeyPush(v, keyCode, event, findText, products, id2);
            }
        });
    }

    protected void setProduct(String result, int titleText, int priceText, int descriptionText, int image) throws JSONException {
        try {
            JSONObject jsonObject = new JSONObject(result);
            setTextInTextView(titleText, jsonObject.getString("title"));
            setTextInTextView(priceText, jsonObject.getString("price"));
            setTextInTextView(descriptionText, jsonObject.getString("description"));
            ImageView productImage = findViewById(image);
            Glide.with(getApplicationContext()).load(jsonObject.getString("img_path") + "?raw=true").into(productImage);
        }
        catch (Exception err) { showErrorMessage(this); };
    }

    public class GetProductTask extends AsyncTask<String, Void, String> {
        private final int titleText;
        private final int priceText;
        private final int descriptionText;
        private final int image;

        public GetProductTask(int titleText, int priceText, int descriptionText, int image) {
            this.titleText = titleText;
            this.priceText = priceText;
            this.descriptionText = descriptionText;
            this.image = image;
        }

        @Override
        protected String doInBackground(String... urls) {
            return HTTPHelper.createConnectionAndReadData(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                setProduct(result, titleText, priceText, descriptionText, image);
            } catch (JSONException ex) {
                showErrorMessage(BaseActivity.this);
            }
        }
    }

    protected View createProductCard(Product product) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View card = inflater.inflate(R.layout.product, null);

        TextView cardTitle = card.findViewById(R.id.product_title);
        cardTitle.setText(product.title);

        TextView noteText = card.findViewById(R.id.product_description);
        noteText.setText(
                (product.description.length() > 30)
                        ? product.description.substring(0, 30) + "..."
                        : product.description);

        ImageView imageView = card.findViewById(R.id.product_img);
        Glide.with(this).load(product.photoPath + "?raw=true").into(imageView);

        card.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ProductDescriptionActivity.class);
            intent.putExtra("user_id", getIntent().getIntExtra("user_id", 0));
            intent.putExtra("product_id", product.product_id);
            view.getContext().startActivity(intent);});

        return card;
    }

    protected void addFavourity(View view, int id) {
        JSONObject object = new JSONObject();
        try {
            object.put("user_id", getIntent().getIntExtra("user_id", 0));
            object.put("product_id", id);
            new HTTPHelper.PostJsonRequestTask(object).execute(HTTPHelper.baseUrl + "/favority");
        } catch (JSONException e) { showErrorMessage(this); }
    }

    protected void deleteFavority(View view, int id) {
        JSONObject object = new JSONObject();
        try {
            object.put("user_id", getIntent().getIntExtra("user_id", 0));
            object.put("product_id", id);
            new HTTPHelper.DeleteJsonRequestTask(object).execute(HTTPHelper.baseUrl + "/favority");
        } catch (JSONException e) { showErrorMessage(this); }
    }
}
