package com.example.stringservice.model;

import java.io.Serializable;
import java.util.Objects;

public class UserTagId implements Serializable {
    private Long user;
    private Long tag;
    
    public UserTagId() {}
    
    public UserTagId(Long user, Long tag) {
        this.user = user;
        this.tag = tag;
    }
    
    public Long getUser() {
        return user;
    }
    
    public void setUser(Long user) {
        this.user = user;
    }
    
    public Long getTag() {
        return tag;
    }
    
    public void setTag(Long tag) {
        this.tag = tag;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTagId)) return false;
        UserTagId that = (UserTagId) o;
        return Objects.equals(user, that.user) && Objects.equals(tag, that.tag);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, tag);
    }
}