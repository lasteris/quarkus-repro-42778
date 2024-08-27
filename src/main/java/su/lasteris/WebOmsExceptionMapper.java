package su.lasteris;


import jakarta.annotation.Priority;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;


@Priority(1)
public class WebOmsExceptionMapper implements ResponseExceptionMapper<RestClientException> {

    @Override
    public RestClientException toThrowable(Response response) {
        String responseEntity =response.readEntity(String.class);
        String entity = responseEntity.isEmpty() ? response.getStatusInfo().getReasonPhrase() : responseEntity;
        return new RestClientException(entity, response.getStatus());
    }

}
