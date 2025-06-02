package eu.europa.ec.cc.processcentre.repository;

import eu.europa.ec.cc.processcentre.repository.model.DeleteProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.FindProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.FindProcessVariableQueryResponse;
import eu.europa.ec.cc.processcentre.repository.model.InsertOrUpdateProcessVariableQueryParam;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProcessVariableMapper {

  void insertOrUpdateProcessVariables(Set<InsertOrUpdateProcessVariableQueryParam> variables);

  void deleteProcessVariables(Set<DeleteProcessVariableQueryParam> variables);

  FindProcessVariableQueryResponse findProcessVariable(FindProcessVariableQueryParam param);

}
