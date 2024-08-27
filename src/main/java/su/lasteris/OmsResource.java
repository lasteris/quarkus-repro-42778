package su.lasteris;

import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Path("api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OmsResource {

    @RestClient
    TypicalApi typicalApi;

    @Inject
    Vertx vertx;

    @GET
    @Path("/slow1000")
    public Uni<String> getSlowly() {
        return Uni.createFrom().emitter(emitter -> vertx.setTimer(1000,
                val -> emitter.complete("i am slowww..")));
    }

    @PATCH
    @Path("/slow")
    public void slow()  {
        int callAmount = 6;
        ExecutorService executorService = Executors.newFixedThreadPool(callAmount);
        CountDownLatch latch = new CountDownLatch(callAmount);
        for (int i = 0; i < callAmount; i++) {
            int finalI = i;
            executorService.execute(() -> {
                long start = System.currentTimeMillis();
                typicalApi.longRunningOperation();
                long time = System.currentTimeMillis() - start;
                System.out.printf("longRunningOperation-%s timing %s%n", finalI + 1, time);

                if (time > 1500) {
                    throw new IllegalStateException();
                }

                latch.countDown();
            });
        }
    }

}
