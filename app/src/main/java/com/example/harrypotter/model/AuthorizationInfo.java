package com.example.harrypotter.model;

public class AuthorizationInfo {
    public int client_id;
    public String email;
    public String password;
    public int is_admin;

    public AuthorizationInfo(int client_id, String email, String password, int is_admin) {
        this.client_id = client_id;
        this.email = email;
        this.password = password;
        this.is_admin = is_admin;
    }
}
