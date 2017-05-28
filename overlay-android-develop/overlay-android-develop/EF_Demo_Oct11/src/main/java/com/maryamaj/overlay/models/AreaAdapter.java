package com.maryamaj.overlay.models;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import io.realm.Realm;


public class AreaAdapter extends TypeAdapter<Area> {

    @Override
    public void write(JsonWriter writer, Area value) throws IOException {
        writer.value(value.getUuid());
    }

    @Override
    public Area read(JsonReader reader) throws IOException {
        String area_uuid = reader.nextString();
        Realm realm = Realm.getDefaultInstance();
        Area area = realm.where(Area.class).equalTo("uuid", area_uuid).findFirst();
        realm.close();
        return area;
    }
}