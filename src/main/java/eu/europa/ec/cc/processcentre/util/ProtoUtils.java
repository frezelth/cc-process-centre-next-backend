package eu.europa.ec.cc.processcentre.util;

import com.google.protobuf.Timestamp;
import java.time.Instant;

public class ProtoUtils {

  public static Instant timestampToInstant(Timestamp timestamp) {
    if (timestamp == null || timestamp.getSeconds() == 0) {
      return null;
    }
    return Instant.ofEpochSecond(timestamp.getSeconds(),
        timestamp.getNanos());
  }

  public static Timestamp instantToTimestamp(Instant instant) {
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

}
