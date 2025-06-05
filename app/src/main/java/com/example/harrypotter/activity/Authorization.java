package com.example.harrypotter.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.harrypotter.HTTPHelper;
import com.example.harrypotter.R;
import com.example.harrypotter.SHA256;
import com.example.harrypotter.model.AuthorizationInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Authorization extends BaseActivity {
    protected ArrayList<AuthorizationInfo> authorizationInformation = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.authorization);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button registrationButton = findViewById(R.id.registration_button);
        registrationButton.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), Registration.class);
            view.getContext().startActivity(intent);
        });

        EditText emailInput = findViewById(R.id.email_input);
        EditText passwordInput = findViewById(R.id.password_input);
        TextView errorText = findViewById(R.id.error_text);



        // Получаем пароли и логины пользователей
        new GetAuthorizationTask().execute(HTTPHelper.baseUrl + "/users");

        // При нажатии на кнопку авторизоваться
        Button authorizationButton = findViewById(R.id.authorization_button);
        authorizationButton.setOnClickListener(view -> {
            errorText.setText("");
            String email = emailInput.getText().toString();
            // Переводим пароль в хэш для проверки
            String password = SHA256.calculateSHA256(passwordInput.getText().toString());

            boolean findUser = false;
            // Сравниваем логин и пароль со всеми логинами и паролями пользователей
            for (AuthorizationInfo authorizationInfo: authorizationInformation) {
                if (email.equals(authorizationInfo.email) && password.equals(authorizationInfo.password)) {
                    findUser = true;
                    Intent intent;
                    if (authorizationInfo.is_admin == 1) {
                        intent = new Intent(view.getContext(), AddProduct.class);
                    } else {
                        intent = new Intent(view.getContext(), MainActivity.class);
                    }

                    // Сохраняем id вошедшего в приложение пользователя
                    intent.putExtra("user_id", authorizationInfo.client_id);
                    view.getContext().startActivity(intent);
                    break;
                }
            }
            if (!findUser) { errorText.setText("Неверный логин или пароль!"); }
        });
    }

    // Получение авторизационных данных
    public class GetAuthorizationTask extends AsyncTask<String, Void, String> {
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

                    // Для каждого пользователя получаем его id, почту и пароль
                    int user_id = jsonObject.getInt("id");
                    String email = jsonObject.getString("email");
                    String password = jsonObject.getString("password");
                    int is_admin = jsonObject.getInt("is_admin");

                    AuthorizationInfo authorizationInfo = new AuthorizationInfo(user_id, email, password, is_admin);
                    authorizationInformation.add(authorizationInfo);
                }
            } catch (JSONException e) { }
        }
    }
}