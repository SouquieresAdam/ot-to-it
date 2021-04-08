package com.cgi.connect;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.cgi.connect.config.ConnectionManager;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.metadata.PlcConnectionMetadata;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class PLCSubscriptionSourceTaskTest {

  private PLCSubscriptionSourceTask task;

  final SourceTaskContext context = EasyMock.createNiceMock(SourceTaskContext.class);

  @BeforeEach
  public void setup() {
    this.task = new PLCSubscriptionSourceTask();
    this.task.initialize(context);
  }

  @Test
  public void shouldBuildSomeNewEventWhenSubscriptionValueChanges()
      throws PlcConnectionException, InterruptedException {

    try (MockedStatic<ConnectionManager> mockedConnectionManager =
        mockStatic(ConnectionManager.class)) {

      var sourceProperties = new HashMap<String, String>();
      sourceProperties.put(PLCSubscriptionSourceConfig.SUBSCRIPTIONS, "machine1,machine2,machine3");
      sourceProperties.put(
          PLCSubscriptionSourceConfig.OUTPUT_FIELDS, "id,date,fabricationId,weight");
      sourceProperties.put(PLCSubscriptionSourceConfig.OUTPUT_KEY, "id,fabricationId");
      sourceProperties.put(PLCSubscriptionSourceConfig.PLC_CONNECTION_STRING, "myConnectionString");
      sourceProperties.put(PLCSubscriptionSourceConfig.KAFKA_TOPIC, "output_topic");
      sourceProperties.put("plc.subscriptions.machine1.path", "ns=2;s=Fabrication_Id_1:STRING");
      sourceProperties.put("plc.subscriptions.machine2.path", "ns=2;s=Fabrication_Id_2:STRING");
      sourceProperties.put("plc.subscriptions.machine3.path", "ns=2;s=Fabrication_Id_3:STRING");
      sourceProperties.put("plc.mappings.machine1.id.path", "ns=2;s=Id_1:STRING");
      sourceProperties.put("plc.mappings.machine1.date.path", "ns=2;s=Fabrication_Date_1:STRING");
      sourceProperties.put(
          "plc.mappings.machine1.fabricationId.path", "ns=2;s=Fabrication_Id_1:STRING");
      sourceProperties.put(
          "plc.mappings.machine1.weight.path", "ns=2;s=Fabrication_Weight_1:STRING");

      sourceProperties.put("plc.mappings.machine2.id.path", "ns=2;s=Id_2:STRING");
      sourceProperties.put("plc.mappings.machine2.date.path", "ns=2;s=Fabrication_Date_2:STRING");
      sourceProperties.put(
          "plc.mappings.machine2.fabricationId.path", "ns=2;s=Fabrication_Id_2:STRING");
      sourceProperties.put(
          "plc.mappings.machine2.weight.path", "ns=2;s=Fabrication_Weight_2:STRING");
      sourceProperties.put("plc.mappings.machine3.id.path", "ns=2;s=Id_3:STRING");
      sourceProperties.put("plc.mappings.machine3.date.path", "ns=2;s=Fabrication_Date_3:STRING");
      sourceProperties.put(
          "plc.mappings.machine3.fabricationId.path", "ns=2;s=Fabrication_Id_3:STRING");
      sourceProperties.put(
          "plc.mappings.machine3.weight.path", "ns=2;s=Fabrication_Weight_3:STRING");

      // Prepare mocks
      PlcConnection connectionMock = mock(PlcConnection.class);
      PlcReadRequest.Builder readRequestBuilderMock = mock(PlcReadRequest.Builder.class);
      PlcReadRequest readRequestMock = mock(PlcReadRequest.class);
      PlcReadResponse response = mock(PlcReadResponse.class);
      PlcSubscriptionHandle subscriptionHandleMock = mock(PlcSubscriptionHandle.class);

      PlcConnectionMetadata metadata =
          new PlcConnectionMetadata() {
            @Override
            public boolean canRead() {
              return true;
            }

            @Override
            public boolean canWrite() {
              return true;
            }

            @Override
            public boolean canSubscribe() {
              return true;
            }
          };
      CompletableFuture readFuture = new CompletableFuture<PlcReadResponse>();
      CompletableFuture readFuture2 = new CompletableFuture<PlcReadResponse>();
      CompletableFuture readFuture3 = new CompletableFuture<PlcReadResponse>();

      // Define mock behaviour
      mockedConnectionManager
          .when(() -> ConnectionManager.getConnection(anyString()))
          .thenReturn(connectionMock);
      when(connectionMock.getMetadata()).thenReturn(metadata);
      when(connectionMock.readRequestBuilder()).thenReturn(readRequestBuilderMock);
      when(readRequestBuilderMock.build()).thenReturn(readRequestMock);
      when(readRequestMock.execute()).thenReturn(readFuture, readFuture2, readFuture3);
      when(response.getString(anyString()))
          .thenReturn("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

      var runner = new PollRunner();
      runner.configure(sourceProperties);
      runner.run();

      readFuture.complete(response);
      readFuture2.complete(response);
      readFuture3.complete(response);

      // Verify mocks

      // 12 fields requested with read requests (4 for each subscription)
      verify(readRequestBuilderMock, times(12)).addItem(anyString(), anyString());

      var result = runner.getNewEvents();
      assertEquals(result.size(), 0);

      runner.run(); // Second run : data should be updated
      readFuture.complete(response);
      readFuture2.complete(response);
      readFuture3.complete(response);

      result = runner.getNewEvents();
      assertEquals(result.size(), 3);

      var sourceRecord = result.get(0);
      var data = (Struct) sourceRecord.value();

      assertEquals(sourceRecord.topic(), "output_topic");
      assertEquals(sourceRecord.key(), "8#5");
      assertEquals(data.get("id"), "8");
      assertEquals(data.get("date"), "6");
      assertEquals(data.get("fabricationId"), "5");
      assertEquals(data.get("weight"), "7");
    }
  }
}
