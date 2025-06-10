package eu.europa.ec.cc.processcentre.process.command.repository;

import eu.europa.ec.cc.processcentre.process.command.repository.model.CompleteTaskQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.CreateTaskQueryParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper {

    void insertOrUpdateTask(CreateTaskQueryParam param);

    void completeTask(CompleteTaskQueryParam param);

}
