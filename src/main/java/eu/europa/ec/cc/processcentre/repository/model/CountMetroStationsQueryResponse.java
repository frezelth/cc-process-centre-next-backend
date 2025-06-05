package eu.europa.ec.cc.processcentre.repository.model;

public record CountMetroStationsQueryResponse(
    String stationName,
    int ongoingProcesses,
    int completedProcesses,
    int ongoingTasks,
    int processesInBusinessState
) {

}
