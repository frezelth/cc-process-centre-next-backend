package eu.europa.ec.cc.processcentre.process.command.service;

import eu.europa.ec.cc.processcentre.config.AccessRight;
import eu.europa.ec.cc.processcentre.config.AccessRight.Right;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessSecurityQueryParam;
import eu.europa.ec.cc.processcentre.template.TemplateExtractor;
import eu.europa.ec.cc.processcentre.template.TemplateService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class ProcessSecurityService {

  private final ProcessMapper processMapper;
  private final TemplateService templateService;

  public ProcessSecurityService(ProcessMapper processMapper,
      TemplateService templateService) {
    this.processMapper = processMapper;
    this.templateService = templateService;
  }

  @Transactional
  public void updateProcessSecurity(
      String processInstanceId,
      ProcessTypeConfig processTypeConfig){
    //TODO support only one processTypeConfig task to simplify ???
    List<AccessRight> accessRights = processTypeConfig.accessRights().getOrDefault(Right.VIEW,
        Collections.emptyList());

    if (accessRights.isEmpty() && StringUtils.isEmpty(processTypeConfig.secundaTask())){
      return;
    }

    UpdateProcessSecurityQueryParam param;
    if (!accessRights.isEmpty()) {
      param = accessRights.stream().findFirst().map(
          right -> fillProcessSecurityQueryParam(processInstanceId, right)
      ).orElseThrow();
    } else {
      param = new UpdateProcessSecurityQueryParam(
          processInstanceId,
          AccessRight.PROCESS_CENTRE,
          processTypeConfig.secundaTask(),
          null, null, null
      );
    }
    processMapper.updateProcessSecurity(param);
  }

  private UpdateProcessSecurityQueryParam fillProcessSecurityQueryParam(String processInstanceId, AccessRight right){
    String scopeIdVal = null;
    String responsibleOrganisationIdVal = null;
    if (StringUtils.isNotEmpty(right.scopeTypeId()) && StringUtils.isNotEmpty(right.scopeId())){
      scopeIdVal = getScopeIdVal(processInstanceId, right.scopeId());
    }
    if (StringUtils.isNotEmpty(right.organisationId())){

    }
    return new UpdateProcessSecurityQueryParam(
        processInstanceId,
        StringUtils.isNotEmpty(right.applicationId()) ? right.applicationId() : AccessRight.PROCESS_CENTRE,
        right.permissionId(),
        right.scopeTypeId(),
        right.scopeId(),
        scopeIdVal
    );
  }

  public @Nullable String getResponsibleOrganisationIdVal(String processInstanceId){
    return null;
  }

  private @Nullable String getScopeIdVal(String processInstanceId, String scopeId) {
    Set<String> usedVariables = TemplateExtractor.extractVariables(scopeId);

    if (!CollectionUtils.isEmpty(usedVariables)){
      // support only one variable
      String processVariableName = usedVariables.iterator().next();
      if (processVariableName.startsWith(ProcessTypeConfig.PROCESS_VARIABLES_PREFIX)){
        processVariableName = processVariableName.substring(ProcessTypeConfig.PROCESS_VARIABLES_PREFIX.length());
      }

      FindProcessVariableQueryResponse processVariable = processMapper.findProcessVariable(
          new FindProcessVariableQueryParam(
              processInstanceId, processVariableName
          ));
      return processVariable.getValueString();
    } else {
      return scopeId;
    }
  }
}
