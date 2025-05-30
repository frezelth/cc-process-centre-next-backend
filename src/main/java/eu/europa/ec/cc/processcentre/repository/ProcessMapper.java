package eu.europa.ec.cc.processcentre.repository;

import eu.europa.ec.cc.processcentre.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.repository.model.SearchProcessQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.SearchProcessQueryResponse;
import eu.europa.ec.cc.processcentre.repository.model.UpdateProcessSecurityQueryParam;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProcessMapper {

  void ensureProcessExists(String processInstanceId);

  void insertOrUpdateProcess(CreateProcessQueryParam process);

  Optional<FindProcessByIdQueryResponse> findById(String id);

  List<SearchProcessQueryResponse> search(SearchProcessQueryParam param);

  void updateProcessSecurity(UpdateProcessSecurityQueryParam param);
}
