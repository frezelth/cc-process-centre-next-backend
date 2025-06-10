package eu.europa.ec.cc.processcentre.process.command.repository;

import eu.europa.ec.cc.processcentre.process.command.repository.model.CancelProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertOrUpdateProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessSecurityQueryParam;
import java.util.Optional;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProcessMapper {

  Optional<FindProcessByIdQueryResponse> findById(String id);

  void insertOrUpdateProcess(CreateProcessQueryParam process);

  void updateProcessSecurity(UpdateProcessSecurityQueryParam param);

  void cancelProcess(CancelProcessQueryParam param);

  void deleteProcess(DeleteProcessQueryParam deleteProcessQueryParam);

  void insertOrUpdateProcessVariables(Set<InsertOrUpdateProcessVariableQueryParam> variables);

  void deleteProcessVariables(Set<DeleteProcessVariableQueryParam> variables);

  FindProcessVariableQueryResponse findProcessVariable(FindProcessVariableQueryParam param);

}
