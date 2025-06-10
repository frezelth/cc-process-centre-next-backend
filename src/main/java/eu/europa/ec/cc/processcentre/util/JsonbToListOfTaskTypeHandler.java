package eu.europa.ec.cc.processcentre.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponseTask;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class JsonbToListOfTaskTypeHandler extends
    BaseTypeHandler<List<SearchProcessQueryResponseTask>> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  @SneakyThrows
  public void setNonNullParameter(PreparedStatement ps, int i, List<SearchProcessQueryResponseTask> parameter, JdbcType jdbcType) throws SQLException {
    ps.setObject(i, objectMapper.writeValueAsString(parameter), Types.OTHER);
  }

  @Override
  public List<SearchProcessQueryResponseTask> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String json = rs.getString(columnName);
    if (json == null) {
      return null;
    }
    try {
      return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, SearchProcessQueryResponseTask.class));
    } catch (Exception e) {
      throw new SQLException("Failed to parse JSONB column: " + columnName, e);
    }
  }

  @Override
  public List<SearchProcessQueryResponseTask> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String json = rs.getString(columnIndex);
    if (json == null) {
      return null;
    }
    try {
      return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, SearchProcessQueryResponseTask.class));
    } catch (Exception e) {
      throw new SQLException("Failed to parse JSONB column: " + columnIndex, e);
    }
  }

  @Override
  public List<SearchProcessQueryResponseTask> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String json = cs.getString(columnIndex);
    if (json == null) {
      return null;
    }
    try {
      return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, SearchProcessQueryResponseTask.class));
    } catch (Exception e) {
      throw new SQLException("Failed to parse JSONB column: " + columnIndex, e);
    }
  }
}