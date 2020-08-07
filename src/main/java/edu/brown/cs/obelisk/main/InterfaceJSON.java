package edu.brown.cs.obelisk.main;

import com.google.gson.*;

import java.lang.reflect.Type;

public class InterfaceJSON implements JsonDeserializer<Object>, JsonSerializer<Object> {
  private static final String CLASS_META_KEY = "CLASS_META_KEY";

  @Override
  public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    JsonObject obj = jsonElement.getAsJsonObject();
    String className = obj.get(CLASS_META_KEY).getAsString();
    try {
      Class<?> clz = Class.forName(className);
      return jsonDeserializationContext.deserialize(jsonElement, clz);
    } catch (ClassNotFoundException e) {
      throw new JsonParseException(e);
    }
  }

  @Override
  public JsonElement serialize(Object obj, Type type, JsonSerializationContext jsonSerializationContext) {
    JsonElement elt = jsonSerializationContext.serialize(
          obj, obj.getClass());
    elt.getAsJsonObject().addProperty(
          CLASS_META_KEY, obj.getClass().getCanonicalName());
    return elt;
  }
}
