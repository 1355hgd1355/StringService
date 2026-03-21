package com.example.stringservice.model;

import java.io.Serializable;
import java.util.Objects;

public class UserSourceId implements Serializable {
    private Long user;
    private Long source;
    
    public UserSourceId() {}
    
    public UserSourceId(Long user, Long source) {
        this.user = user;
        this.source = source;
    }
    
    public Long getUser() {
        return user;
    }
    
    public void setUser(Long user) {
        this.user = user;
    }
    
    public Long getSource() {
        return source;
    }
    
    public void setSource(Long source) {
        this.source = source;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSourceId)) return false;
        UserSourceId that = (UserSourceId) o;
        return Objects.equals(user, that.user) && Objects.equals(source, that.source);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, source);
    }
}