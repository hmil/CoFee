package fr.hmil.cofee.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Count {

    private final String name;
    private final int id;

    public Count(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Count fromJson(JSONObject obj) throws JSONException {
        Count res = new Count(obj.getInt("id"), obj.getString("name"));

        return res;
    }

    @Override
    public String toString() {
        return name;
    }
}
