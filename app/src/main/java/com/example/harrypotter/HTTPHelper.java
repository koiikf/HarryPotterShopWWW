package com.example.harrypotter;

import android.os.AsyncTask;

import com.example.harrypotter.model.Product;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPHelper {
    public static String baseUrl = "https://harry-potter-api-koiikf.amvera.io/api/v1";

    // Чтение данных GET запроса
    public static String createConnectionAndReadData(String connectionUrl) {
        try {
            URL url = new URL(connectionUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream responseStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    // Создание соендинения для запросов неа добавление, редактирование и удаление данных
    public static String createConnectionData(String connectionUrl, JSONObject jsonBody, String requestType) {
        try {
            URL url = new URL(connectionUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestType);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(jsonBody.toString());
            writer.flush();
            writer.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();

        } catch (Exception e) {}
        return null;
    }

    // базовый класс запроса
    public static class JsonRequestTask extends AsyncTask<String, Void, String> {
        private JSONObject jsonBody;
        private String method;

        public JsonRequestTask(JSONObject jsonBody, String method) {
            this.jsonBody = jsonBody;
            this.method = method;
        }

        @Override
        protected String doInBackground(String... urls) {
            return HTTPHelper.createConnectionData(urls[0], jsonBody, method);
        }
    }

    // Для запроса на редктирование устанвливаем метод PATCH
    public static class PatchJsonRequestTask extends JsonRequestTask {
        public PatchJsonRequestTask(JSONObject jsonBody) {
            super(jsonBody, "PATCH");
        }
    }

    // Для удаление DELETE
    public static class DeleteJsonRequestTask extends JsonRequestTask {
        public DeleteJsonRequestTask(JSONObject jsonBody) {
            super(jsonBody, "DELETE");
        }
    }

    // Для добавления POST
    public static class PostJsonRequestTask extends JsonRequestTask {
        public PostJsonRequestTask(JSONObject jsonBody) {
            super(jsonBody, "POST");
        }
    }


    // Создание объекта класса Note
    public static Product createProduct(JSONObject jsonObject) throws JSONException {
        int product_id = jsonObject.getInt("id");
        String title = jsonObject.getString("title");
        String description = jsonObject.getString("description");
        String imgPath = jsonObject.getString("img_path");
        int price = jsonObject.getInt("price");
        return new Product(product_id, title, description, imgPath, price);
    }
}
