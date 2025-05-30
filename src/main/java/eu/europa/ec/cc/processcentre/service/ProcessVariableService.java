package eu.europa.ec.cc.processcentre.service;

import eu.europa.ec.cc.processcentre.repository.model.DeleteProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.InsertOrUpdateProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessVariables;
import eu.europa.ec.cc.processcentre.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.repository.ProcessVariableMapper;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.variables.proto.VariableValue;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessVariableService {

  private static final String MIME_TYPE_JSON = "application/json";

  private final ProcessVariableMapper processVariableMapper;
  private final ProcessMapper processMapper;

  public ProcessVariableService(ProcessVariableMapper processVariableMapper,
      ProcessMapper processMapper) {
    this.processVariableMapper = processVariableMapper;
    this.processMapper = processMapper;
  }

  @Transactional
  public void updateVariables(UpdateProcessVariables command) {

    Set<InsertOrUpdateProcessVariableQueryParam> variablesToInsertOrUpdate = command.getProcessVariablesMap()
        .entrySet().stream()
        .filter(entry -> !entry.getValue().getDeleted())
        .map(
            e -> buildInsertOrUpdateProcessVariableDto(
                command.getProcessInstanceId(),
                e.getKey(),
                e.getValue()
            )
        ).filter(Objects::nonNull).collect(Collectors.toSet());
    if (!variablesToInsertOrUpdate.isEmpty()) {
      // first we should ensure that the process already exists (out of order messages)
      processMapper.ensureProcessExists(command.getProcessInstanceId());
      processVariableMapper.insertOrUpdateProcessVariables(variablesToInsertOrUpdate);
    }

    Set<DeleteProcessVariableQueryParam> variablesToDelete = command.getProcessVariablesMap()
        .entrySet().stream()
        .filter(entry -> entry.getValue().getDeleted())
        .map(
            e -> new DeleteProcessVariableQueryParam(command.getProcessInstanceId(), e.getKey())
        ).collect(Collectors.toSet());

    if (!variablesToDelete.isEmpty()) {
      processVariableMapper.deleteProcessVariables(variablesToDelete);
    }
  }

  private InsertOrUpdateProcessVariableQueryParam buildInsertOrUpdateProcessVariableDto(
      String processInstanceId,
      String name,
      VariableValue value
  ){

    return switch (value.getKindCase()){
      case STRINGVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, null, value.getStringValue(), null, null,
          null, null, null, null, null
      );
      case BOOLEANVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, null, null, null, null,
          null, value.getBooleanValue(), null, null, null
      );
      case INTEGERVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, value.getIntegerValue(), null, null, null,
          null, null, null, null, null
      );
      case LONGVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, null, null, value.getLongValue(), null,
          null, null, null, null, null
      );
      case DOUBLEVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, null, null, null, value.getDoubleValue(),
          null, null, null, null, null
      );
      case TIMEVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, null, null, null, null,
          ProtoUtils.timestampToInstant(value.getTimeValue()), null, null, null, null
      );
      case BYTESVALUE -> {
        if (value.getMimeType().equals(MIME_TYPE_JSON)) {
          yield  new InsertOrUpdateProcessVariableQueryParam(
              processInstanceId,
              name, null, null, null, value.getDoubleValue(),
              null, null, new String(value.getBytesValue().toByteArray(), StandardCharsets.UTF_8), null, null
          );
        }
        yield new InsertOrUpdateProcessVariableQueryParam(
            processInstanceId,
          name, null, null, null, null,
          null, null, null, value.getMimeType(), value.toByteArray()
        );
      }
      case DELETED, KIND_NOT_SET -> null;
    };

  }
}
