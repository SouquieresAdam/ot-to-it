package com.cgi.connect.config;

import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;

public class ConnectionManager {

  /**
   * @param connectionString the connection string to PlcConnection
   * @returna PlcConnection
   * @throws PlcConnectionException
   */
  public static PlcConnection getConnection(String connectionString) throws PlcConnectionException {
    return new PlcDriverManager().getConnection(connectionString);
  }
}
