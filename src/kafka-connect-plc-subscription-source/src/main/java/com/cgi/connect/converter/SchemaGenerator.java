package com.cgi.connect.converter;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cgi.connect.PLCSubscriptionSourceConfig.OUTPUT_FIELDS;

public class SchemaGenerator {
    public static Schema generate(Map<String, String> config) {

       var schemaBuilder = SchemaBuilder.struct();
       var fieldDefinitions = config.entrySet().stream()
               .filter( e -> e.getKey().contains(OUTPUT_FIELDS))
               .map( e -> Pair.of(e.getKey().replaceFirst(OUTPUT_FIELDS+".", ""), e.getValue()))
               .collect(Collectors.toList());


       var treeElements = new HashMap<String, TreeElement>();

       // Create all tree elements
        fieldDefinitions.stream()
                .flatMap( fieldEntry -> Arrays.stream(fieldEntry.getKey().split("\\.")))
                .forEach(subField -> treeElements.computeIfAbsent(subField, TreeElement::of));

        // Hierarchy Building
        fieldDefinitions.forEach(fieldDefinition -> {
            var fieldSplit = fieldDefinition.getKey().split("\\.");

            for (int i = 0; i<fieldSplit.length; i++) {

                var currentField = treeElements.get(fieldSplit[i]);

                if(i ==  fieldSplit.length - 1) {
                    // if it's the last
                    currentField.setType(fieldDefinition.getValue());
                } else {
                    currentField.setType("STRUCT");
                }

                if(i>0) {

                    var previousField = treeElements.get(fieldSplit[i-1]);

                    // if not the first
                    currentField.setParent(previousField);
                    previousField.addChild(currentField);
                }
            }
        });

        var firstLevelElements = treeElements.values().stream()
                .filter(TreeElement::isRoot)
                .collect(Collectors.toList());

        firstLevelElements.forEach( el -> buildField(el, schemaBuilder));

       return schemaBuilder.build();
    }

    private static void buildField(TreeElement el, SchemaBuilder builder) {


        if(el.getChildren().size() > 0) {
            // If it's a sub struct
            var subBuilder = SchemaBuilder.struct();
            el.getChildren().forEach( child -> buildField(child, subBuilder));
            builder.field(el.getId(), subBuilder.build());
            return;
        }


        switch(el.getType()) {
            case "STRING":
            default:
                builder.field(el.getId(), Schema.STRING_SCHEMA);
            //TODO Handle some more data types
        }
    }
}
