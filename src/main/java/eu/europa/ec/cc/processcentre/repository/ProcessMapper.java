package eu.europa.ec.cc.processcentre.repository;

import eu.europa.ec.cc.processcentre.repository.model.*;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProcessMapper {

  void insertOrUpdateProcess(CreateProcessQueryParam process);

  void updateProcessSecurity(UpdateProcessSecurityQueryParam param);

  void cancelProcess(CancelProcessQueryParam param);

  void deleteProcess(DeleteProcessQueryParam deleteProcessQueryParam);
}
