package eu.europa.ec.cc.processcentre.task.repository;

import eu.europa.ec.cc.processcentre.task.repository.model.ChangeStatusQueryParam;
import eu.europa.ec.cc.processcentre.task.repository.model.CreateTaskQueryParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper {

    void insertOrUpdateTask(CreateTaskQueryParam param);

    void changeStatus(ChangeStatusQueryParam param);

    void deleteTask(String taskId);

}
