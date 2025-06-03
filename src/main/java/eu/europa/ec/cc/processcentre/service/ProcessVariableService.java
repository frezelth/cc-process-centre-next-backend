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


  private final ProcessVariableMapper processVariableMapper;
  private final ProcessMapper processMapper;

  public ProcessVariableService(ProcessVariableMapper processVariableMapper,
      ProcessMapper processMapper) {
    this.processVariableMapper = processVariableMapper;
    this.processMapper = processMapper;
  }

  @Transactional
  public void updateVariables(UpdateProcessVariables command) {

  }

}
