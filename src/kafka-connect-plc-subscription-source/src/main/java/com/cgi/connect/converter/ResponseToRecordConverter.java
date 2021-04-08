package com.cgi.connect.converter;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.plc4x.java.api.messages.PlcReadResponse;

public class ResponseToRecordConverter {

  /**
   * Return a kafka SourceRecord from a PlcReadResponse
   *
   * @param response a PLC read response
   * @return a SourceRecord
   */
  public static SourceRecord convert(
      PlcReadResponse response,
      String kafkaTopic,
      Schema outputSchema,
      List<Pair<String, String>> mapping,
      List<String> keyComposition) {
    Struct outputValue = new Struct(outputSchema);

    mapping.forEach(
        mappingPair ->
            outputValue.put(mappingPair.getKey(), response.getString(mappingPair.getKey())));

    // Build output record key
    var key =
        keyComposition.stream()
            .map(outputValue::getString)
            .reduce(
                "",
                (a, b) -> {
                  if (a.isEmpty()) {
                    return b;
                  }

                  return a + "#" + b;
                });

    return new SourceRecord(
        null, // Collections.singletonMap(filename, checksumValue),
        null, // OFFSET_CHECKMARK,
        kafkaTopic,
        Schema.STRING_SCHEMA,
        key,
        outputSchema,
        outputValue);
  }
}
