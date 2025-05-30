package eu.europa.ec.cc.processcentre.service;

import eu.europa.ec.cc.processcentre.config.AccessRight;
import eu.europa.ec.cc.processcentre.config.AccessRight.Right;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
import eu.europa.ec.cc.processcentre.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.repository.model.UpdateProcessSecurityQueryParam;
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

  public ProcessSecurityService(ProcessMapper processMapper) {
    this.processMapper = processMapper;
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
          right -> new UpdateProcessSecurityQueryParam(
              processInstanceId,
              StringUtils.isNotEmpty(right.applicationId()) ? right.applicationId() : AccessRight.PROCESS_CENTRE,
              right.permissionId(),
              right.scopeTypeId(),
              right.scopeId()
          )
      ).orElseThrow();
    } else {
      param = new UpdateProcessSecurityQueryParam(
          processInstanceId,
          AccessRight.PROCESS_CENTRE,
          processTypeConfig.secundaTask(),
          null, null
      );
    }
    processMapper.updateProcessSecurity(param);
  }
}
