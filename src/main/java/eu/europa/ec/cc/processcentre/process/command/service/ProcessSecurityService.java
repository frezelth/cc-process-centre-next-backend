package eu.europa.ec.cc.processcentre.process.command.service;

import eu.europa.ec.cc.processcentre.config.AccessRight;
import eu.europa.ec.cc.processcentre.config.AccessRight.Right;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessSecurityQueryParam;
import eu.europa.ec.cc.processcentre.template.TemplateService;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    if (StringUtils.isNotEmpty(right.scopeTypeId()) && StringUtils.isNotEmpty(right.scopeId())){
      scopeIdVal = templateService.processAccessRightsScopeIdTemplate(processInstanceId, right.scopeId());
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
}
