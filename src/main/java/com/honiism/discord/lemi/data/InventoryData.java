package com.honiism.discord.lemi.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InventoryData {
        
    @JsonProperty("id")
    private final String id;
    
    @JsonProperty("name")
    private String name;
        
    @JsonProperty("count")
    private long count;

    public InventoryData(@JsonProperty("id") String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }

    public void setName(String name) {
        this.name = name;
    }
            
    public void setCount(long count) {
        this.count = count;
    }
}