package eu.europa.ec.cc.processcentre.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class JsonbToMapTypeHandler extends BaseTypeHandler<Map<String, String>> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  @SneakyThrows
  public void setNonNullParameter(PreparedStatement ps, int i, Map<String, String> parameter, JdbcType jdbcType) throws SQLException {
    ps.setObject(i, objectMapper.writeValueAsString(parameter), Types.OTHER);
  }

  @Override
  public Map<String, String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String json = rs.getString(columnName);
    if (json == null || json.isEmpty()) {
      return new HashMap<>();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
    } catch (Exception e) {
      throw new SQLException("Failed to parse JSONB field", e);
    }
  }

  @Override
  public Map<String, String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String json = rs.getString(columnIndex);
    if (json == null || json.isEmpty()) {
      return new HashMap<>();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
    } catch (Exception e) {
      throw new SQLException("Failed to parse JSONB field", e);
    }
  }

  @Override
  public Map<String, String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String json = cs.getString(columnIndex);
    if (json == null || json.isEmpty()) {
      return new HashMap<>();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
    } catch (Exception e) {
      throw new SQLException("Failed to parse JSONB field", e);
    }
  }
}
