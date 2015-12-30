package edu.gatech.wguo64.module_oauth2.backend.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by guoweidong on 12/29/15.
 */

@Entity
public class Token {

    @Id
    Long id;
    String token;
    @Index
    String userId;

    public Token() {}

    public Token(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
