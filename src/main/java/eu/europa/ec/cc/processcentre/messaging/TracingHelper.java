package eu.europa.ec.cc.processcentre.messaging;

import com.google.common.collect.Lists;
import com.google.protobuf.Message;
import eu.europa.ec.cc.message.proto.CCMessage;
import io.micrometer.tracing.Tracer;
import java.util.Collection;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Component
public class TracingHelper {

  private final Tracer tracing;

  public TracingHelper(Tracer tracing) {
    this.tracing = tracing;
  }

  static List<String> LOGGED_FIELDS = Lists.newArrayList(
    "providerId",
    "processTypeId", "providerId", "domainKey", "processTypeKey",
    "processId", "processInstanceId", "processInstance1Id", "processInstance2Id",
    "taskTypeId", "taskId", "taskInstanceId",
    "serviceTaskId",
    "taxonomyKey",
    "portfolioItemId", "portfolioItemTypeId",
    "babelUrn"
  );


  public void tagMessageType(CCMessage message) {
    String typeUrl = message.getPayload().getTypeUrl();
    if (typeUrl != null && typeUrl.startsWith("type.googleapis.com/")) {
      typeUrl = typeUrl.substring("type.googleapis.com/".length());
    }
    tag("message.payload.type", typeUrl);
  }

  public void tag(Message message){
    tag("message.payload", new String(Base64.encodeBase64(message.toByteArray())));
    message.getAllFields().forEach(
      (fd, v) -> {
        if (LOGGED_FIELDS.contains(fd.getName())) { tag(fd.getName(), String.valueOf(v)); }
      }
    );
  }

  public void tag(String key, Collection<?> values){
    tag(key, CollectionUtils.isEmpty(values) ? "empty" : StringUtils.join(values, ','));
  }

  public void tag(String key, String value){
    if (tracing != null && tracing.currentSpan() != null) {
      tracing.currentSpan().tag(key, value);
    }
  }

  public void error(Exception e) {
    if (tracing != null && tracing.currentSpan() != null) {
      tracing.currentSpan().error(e);
    }
  }

  public void abandon(){
    tracing.currentSpan().abandon();
  }

}
