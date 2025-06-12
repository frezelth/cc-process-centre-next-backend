package eu.europa.ec.cc.processcentre.process.command.repository;

import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessPortfolioItems;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessStateQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertOrUpdateProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessActionLogQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessPortfolioItems;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessStateQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateBusinessStatusQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessResponsibleOrganisationQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessResponsibleUserQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessSecurityQueryParam;
import java.util.Optional;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProcessMapper {

  Optional<FindProcessByIdQueryResponse> findById(String id);

  void insertOrUpdateProcess(InsertProcessQueryParam process);

  void updateProcessSecurity(UpdateProcessSecurityQueryParam param);

  void deleteProcess(DeleteProcessQueryParam deleteProcessQueryParam);

  void insertOrUpdateProcessVariables(Set<InsertOrUpdateProcessVariableQueryParam> variables);

  void deleteProcessVariables(Set<DeleteProcessVariableQueryParam> variables);

  FindProcessVariableQueryResponse findProcessVariable(FindProcessVariableQueryParam param);

  void updateBusinessStatus(UpdateBusinessStatusQueryParam updateBusinessStatusQueryParam);

  void insertProcessActionLog(InsertProcessActionLogQueryParam param);

  void updateResponsibleUser(UpdateProcessResponsibleUserQueryParam updateBusinessStatusQueryParam);

  void insertProcessPortfolioItems(InsertProcessPortfolioItems insertProcessPortfolioItems);

  void deleteProcessPortfolioItems(DeleteProcessPortfolioItems deleteProcessPortfolioItems);

  void insertProcessState(InsertProcessStateQueryParam param);

  void deleteProcessState(DeleteProcessStateQueryParam param);

  void updateResponsibleOrganisation(UpdateProcessResponsibleOrganisationQueryParam updateProcessResponsibleOrganisationQueryParam);
}
