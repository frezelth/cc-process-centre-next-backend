package eu.europa.ec.cc.processcentre.util;

import eu.europa.ec.digit.apigw.TokenBuilder;
import eu.europa.ec.digit.apigw.token.TokenRequester;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class System2SystemTokenRetriever {
  
  private final String euLoginClientId;
  
  private final TokenRequester tokenRequester;
  
  public System2SystemTokenRetriever(String euLoginClientId, String euLoginClientSecret,
      String euLoginTokenUrl) {
    super();
    
    this.euLoginClientId = euLoginClientId;
    
    LOG.debug("Creating TokenRequester for euLoginclientId {}", euLoginClientId);
    try {
      tokenRequester = new TokenBuilder(euLoginTokenUrl)
          .setClientId(euLoginClientId)
          .setClientSecret(euLoginClientSecret)
          .buildTokenGenerator();
      
      LOG.info("TokenRequester created succesfully");
    } catch (Exception e) {
      LOG.error("Error when creating TokenRequester for euLoginClientId: " + euLoginClientId, e);
      throw new TokenGenerationException("unable to generate token", e);
    } 
  }

  public String getSystem2SystemToken(String targetClientId, String resourceUrl) {
    if (LOG.isDebugEnabled()){
      LOG.debug("Trying to get a system to system token for target clientId {} on resource {}", euLoginClientId, resourceUrl);
    }
    String token = tokenRequester.getS2SAccessToken(targetClientId, resourceUrl);
    return token;
  }
  
  class TokenGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TokenGenerationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
