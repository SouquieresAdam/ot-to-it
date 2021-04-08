package com.cgi.connect.config;

import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;

public class ConnectionManager {

  public static PlcConnection getConnection(String connectionString) throws PlcConnectionException {
    return new PlcDriverManager().getConnection(connectionString);
  }
}
