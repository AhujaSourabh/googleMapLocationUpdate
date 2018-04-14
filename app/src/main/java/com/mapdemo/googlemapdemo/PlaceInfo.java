package com.mapdemo.googlemapdemo;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Sourabh on 14/04/18.
 */

public class PlaceInfo {

   private String id, name, address, phoneNumber, attributes;
   private Uri websiteUrl;
   private float rating;
   private LatLng latLng;

    public PlaceInfo(String id, String name, String address,
                     String phoneNumber, String attributes, Uri websiteUrl, float rating,
                     LatLng latLng) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.attributes = attributes;
        this.websiteUrl = websiteUrl;
        this.rating = rating;
        this.latLng = latLng;
    }

    public PlaceInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public Uri getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(Uri websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public String toString() {
        return "PlaceInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", attributes='" + attributes + '\'' +
                ", websiteUrl=" + websiteUrl +
                ", rating=" + rating +
                ", latLng=" + latLng +
                '}';
    }
}
