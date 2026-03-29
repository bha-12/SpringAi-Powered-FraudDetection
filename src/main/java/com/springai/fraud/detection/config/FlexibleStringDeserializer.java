package com.springai.fraud.detection.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlexibleStringDeserializer extends StdDeserializer<String> {

    public FlexibleStringDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctx) 
            throws IOException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            return p.getText();
        }

        if (p.currentToken() == JsonToken.START_ARRAY) {
            List<String> items = new ArrayList<>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                items.add(p.getText());
            }
            return String.join(". ", items);
        }

        return p.getText();
    }
}