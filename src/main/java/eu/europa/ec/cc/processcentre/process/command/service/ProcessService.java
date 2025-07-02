package eu.europa.ec.cc.processcentre.process.command.service;

import eu.europa.ec.cc.processcentre.event.ProcessRunningStatusChanged;
import eu.europa.ec.cc.processcentre.model.ProcessStatus;
import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FavouriteQueryParam;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessService {

  private final ProcessMapper processMapper;

  public ProcessService(ProcessMapper processMapper) {
    this.processMapper = processMapper;
  }

  @Transactional
  public void setAsFavourite(String processInstanceId, String userId){
    this.processMapper.setAsFavourite(
        new FavouriteQueryParam(processInstanceId, userId)
    );
  }

  @Transactional
  public void removeFromFavourites(String processInstanceId, String userId){
    this.processMapper.deleteFromFavourites(
        new FavouriteQueryParam(processInstanceId, userId)
    );
  }

  /**
   * On process completed/cancelled, store the responsible organisation code to avoid changing it
   * it the organisation changes
   * On process reopened, remove the responsible organisation id
   * @param event
   */
  @EventListener
  public void handle(ProcessRunningStatusChanged event){
    if (event.processStatus() == ProcessStatus.COMPLETED || event.processStatus() == ProcessStatus.CANCELLED){
      processMapper.snapshotOnComplete(event.processInstanceId());
    } else {
      processMapper.removeSnapshotOnReopen(event.processInstanceId());
    }
  }

}
