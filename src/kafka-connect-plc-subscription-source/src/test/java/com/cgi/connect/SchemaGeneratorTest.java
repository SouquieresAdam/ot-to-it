package com.cgi.connect;

import com.cgi.connect.converter.SchemaGenerator;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SchemaGeneratorTest {
  @Test
  public void shouldBuildSchema() {

    final Map<String, String> sourceProperties = new HashMap<>();
    sourceProperties.put(PLCSubscriptionSourceConfig.OUTPUT_FIELDS+".id", "STRING");
    sourceProperties.put(PLCSubscriptionSourceConfig.OUTPUT_FIELDS+".date", "STRING");
    sourceProperties.put(PLCSubscriptionSourceConfig.OUTPUT_FIELDS+".fabricationId", "STRING");
    sourceProperties.put(PLCSubscriptionSourceConfig.OUTPUT_FIELDS+".weight", "STRING");
    sourceProperties.put(PLCSubscriptionSourceConfig.OUTPUT_FIELDS+".operation.subField1", "STRING");
    sourceProperties.put(PLCSubscriptionSourceConfig.OUTPUT_FIELDS+".operation.subField2", "STRING_ARRAY");
    sourceProperties.put(PLCSubscriptionSourceConfig.OUTPUT_FIELDS+".operation.subField3", "STRING");
    sourceProperties.put(PLCSubscriptionSourceConfig.OUTPUT_FIELDS+".operation.details.subSubField1", "STRING");

    var fieldTree= SchemaGenerator.buildFieldTree(sourceProperties);
    var result = SchemaGenerator.generate(fieldTree);

    assertEquals( 5, result.fields().size());
    assertEquals( 4, result.field("operation").schema().fields().size());
    assertNotNull(  result.field("operation").schema().field("subField1"));
    assertEquals(  result.field("operation").schema().field("subField2").schema(),SchemaBuilder.array(Schema.STRING_SCHEMA).build());
    assertNotNull(  result.field("operation").schema().field("details"));
    assertNotNull(  result.field("operation").schema().field("details").schema().field("subSubField1"));
  }

}
