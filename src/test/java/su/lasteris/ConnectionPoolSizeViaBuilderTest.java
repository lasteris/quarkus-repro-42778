package su.lasteris;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.client.api.QuarkusRestClientProperties;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ConnectionPoolSizeViaBuilderTest {
    @TestHTTPResource
    URI uri;

    /**
     * This test asserts that 5 calls requires no queue at all.
     * @throws InterruptedException
     */
    @Test
    void shouldPerform5CallsWithoutQueuing() throws InterruptedException {
        Client client = QuarkusRestClientBuilder.newBuilder().baseUri(uri)
                .build(Client.class);

        CountDownLatch latch = executeCalls(client, 5);

        assertThat(latch.await(1200, TimeUnit.MILLISECONDS))
                .overridingErrorMessage("Failed to do 5 calls in 2 seconds")
                .isTrue();
    }

    /**
     * This test proves that poolSize is only 5.
     * Below described motivation of this test.
     * @see org.jboss.resteasy.reactive.client.impl.ClientImpl.DEFAULT_CONNECTION_POOL_SIZE
     * if DEFAULT_CONNECTION_POOL_SIZE parameter were really honored,
     * then up to 20 callAmount must be executed within 1 seconds + little amount of time for the rest of work.
     * but with just only 6 callAmount you will start to get execution time > 2s,
     * and this result proves that poolSize still on level of property below
     * @see io.vertx.core.http.HttpClientOptions.DEFAULT_MAX_POOL_SIZE
     */
    @Test
    void provesThatEven6CallsRequiresQueuing() throws InterruptedException {
        Client client = QuarkusRestClientBuilder.newBuilder().baseUri(uri)
                .build(Client.class);

        long start = System.currentTimeMillis();
        CountDownLatch latch = executeCalls(client, 6);
        latch.await();

        long diff = System.currentTimeMillis() - start;
        assertThat(diff).isLessThan(3000).isGreaterThanOrEqualTo(2000);
        System.out.println(diff);
    }

    /**
     * This test proves that
     * #.property(QuarkusRestClientProperties.CONNECTION_POOL_SIZE, 10) call has no effect,
     * Client is still configured to use queuing when callAmount 6 or above.
     * @throws InterruptedException
     */
    @Test
    void provesThatEven6CallsPropertyRequiresQueuing() throws InterruptedException {
        Client client = QuarkusRestClientBuilder.newBuilder().baseUri(uri)
                .property(QuarkusRestClientProperties.CONNECTION_POOL_SIZE, 10)
                .build(Client.class);

        long start = System.currentTimeMillis();
        CountDownLatch latch = executeCalls(client, 6);
        latch.await();

        long diff = System.currentTimeMillis() - start;
        assertThat(diff).isLessThan(3000).isGreaterThanOrEqualTo(2000);
        System.out.println(diff);
    }

    /**
     * This test proves that manual setup of HttpClientOptions does not work either
     * Client is still configured to use queuing when callAmount 6 or above.
     */
    @Test
    void provesThatEven6CallsHttpClientOptionsRequiresQueuing() throws InterruptedException {
        var options = new HttpClientOptions();
        options.setMaxPoolSize(10);
        Client client = QuarkusRestClientBuilder.newBuilder().baseUri(uri)
                .httpClientOptions(options)
                .build(Client.class);

        long start = System.currentTimeMillis();
        CountDownLatch latch = executeCalls(client, 6);
        latch.await();

        long diff = System.currentTimeMillis() - start;
        assertThat(diff).isLessThan(3000).isGreaterThanOrEqualTo(2000);
        System.out.println(diff);
    }

    private CountDownLatch executeCalls(Client client, int callAmount) {
        ExecutorService executorService = Executors.newFixedThreadPool(callAmount);
        CountDownLatch latch = new CountDownLatch(callAmount);
        for (int i = 0; i < callAmount; i++) {
            executorService.execute(() -> {
                String result = client.get();
                latch.countDown();
                assertThat(result).isEqualTo("i am slowww..");
            });
        }
        return latch;
    }

    @Path("/slow1000")
    public interface Client {
        @GET
        String get();
    }

    @Path("/slow1000")
    public static class SlowResource {
        @Inject
        Vertx vertx;

        @GET
        public Uni<String> getSlowly() {
            return Uni.createFrom().emitter(emitter -> vertx.setTimer(1000,
                    val -> emitter.complete("i am slowww..")));
        }
    }

}