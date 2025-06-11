package eu.europa.ec.cc.processcentre.process.command.repository;

import eu.europa.ec.cc.processcentre.process.command.repository.model.CancelProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.ChangeBusinessStatusQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.ChangeProcessRunningStatusQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertOrUpdateProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessRunningStatusLogQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessSecurityQueryParam;
import java.util.Optional;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProcessMapper {

  Optional<FindProcessByIdQueryResponse> findById(String id);

  void insertOrUpdateProcess(CreateProcessQueryParam process);

  void updateProcessSecurity(UpdateProcessSecurityQueryParam param);

  void deleteProcess(DeleteProcessQueryParam deleteProcessQueryParam);

  void insertOrUpdateProcessVariables(Set<InsertOrUpdateProcessVariableQueryParam> variables);

  void deleteProcessVariables(Set<DeleteProcessVariableQueryParam> variables);

  FindProcessVariableQueryResponse findProcessVariable(FindProcessVariableQueryParam param);

  void changeBusinessStatus(ChangeBusinessStatusQueryParam changeBusinessStatusQueryParam);

  void insertProcessRunningStatusLog(InsertProcessRunningStatusLogQueryParam param);
}
