package com.myapp.partyspot;

/**
 * Created by svaughan on 10/5/14.
 */
public class spotifyPlaylist {
    public String owner;
    public String name;
    public String id;

    public spotifyPlaylist(String name, String id, String owner) {
        this.name = name;
        this.id = id;
        this.owner = owner;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public String getOwner() {
        return this.owner;
    }
}