package com.rdmn.plutus.helperapp.Model;

import com.google.android.gms.maps.model.LatLng;

public class Help {
    LatLng lastLocation;
    String ID;
    Token token;

    public Help() {
    }

    public Help(LatLng lastLocation, String ID, Token token) {
        this.lastLocation = lastLocation;
        this.ID = ID;
        this.token = token;
    }

    public LatLng getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(LatLng lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
