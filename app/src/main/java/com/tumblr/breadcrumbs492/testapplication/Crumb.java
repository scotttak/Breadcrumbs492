package com.tumblr.breadcrumbs492.testapplication;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Aaron on 3/11/2015.
 */
public class Crumb {

    //TODO: Needs support for custom images, and news feed (Google Maps API Info Windows?)


    private LatLng location;
    private String title;
    private String infoSnippet;
    // The map this crumb exists on
    private GoogleMap map;

    public Crumb()
    {
        location = new LatLng(0, 0);
        title = "";
        map = null;
    }

    public Crumb(LatLng loc, String crumbTitle, String snippet, GoogleMap ownedMap)
    {
        location = loc;
        title = crumbTitle;
        infoSnippet = snippet;
        map = ownedMap;

        createCrumb();
    }

    public void createCrumb()
    {
        // adds crumb to the users local map
        map.addMarker(new MarkerOptions().position(location).title(title).snippet(infoSnippet));

        // adds crumb info to the database
    }


}