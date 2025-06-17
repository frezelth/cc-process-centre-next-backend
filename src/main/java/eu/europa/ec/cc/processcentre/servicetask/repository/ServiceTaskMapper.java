package eu.europa.ec.cc.processcentre.servicetask.repository;

import eu.europa.ec.cc.processcentre.servicetask.repository.model.ChangeStatusServiceTaskQueryParam;
import eu.europa.ec.cc.processcentre.servicetask.repository.model.CreateServiceTaskQueryParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceTaskMapper {

  void insertServiceTask(CreateServiceTaskQueryParam createServiceTaskQueryParam);

  void changeStatus(ChangeStatusServiceTaskQueryParam changeStatusServiceTaskQueryParam);
}
