package eu.europa.ec.cc.processcentre.process.command.service;

import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FavouriteQueryParam;
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

}
