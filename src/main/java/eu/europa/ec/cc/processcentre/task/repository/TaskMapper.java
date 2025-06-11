package eu.europa.ec.cc.processcentre.task.repository;

import eu.europa.ec.cc.processcentre.task.repository.model.ChangeTaskStatusQueryParam;
import eu.europa.ec.cc.processcentre.task.repository.model.CreateTaskQueryParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper {

    void insertOrUpdateTask(CreateTaskQueryParam param);

    void changeStatus(ChangeTaskStatusQueryParam param);

}
