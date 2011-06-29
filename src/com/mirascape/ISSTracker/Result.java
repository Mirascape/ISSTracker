package com.mirascape.ISSTracker;

import com.google.gson.annotations.SerializedName;

public class Result 
{
    @SerializedName("id")
    public int id;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("description")
    public String description;
    
    @SerializedName("latitude")
    public Double latitude;
    
    @SerializedName("longitude")
    public Double longitude;
        
    @SerializedName("altitude")
    public Double altitude;    
}
