/*
 * Copyright Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kafkastreams.iot.restserver;

import kafkastreams.iot.model.IotDataMessage;
import kafkastreams.iot.model.IotSensorRules;
import kafkastreams.iot.streams.IotDataAggregator;
import kafkastreams.iot.streams.StreamBuilder;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static kafkastreams.iot.streams.StreamBuilder.AGG_STORE_NAME;

@Path("iot-engine")
public class RestService {

    private final KafkaStreams streams;
    private final MetadataService metadataService;
    private final HostInfo appServer;
    private final HostInfo restServer;
    private final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
    private Server jettyServer;
    private static final Logger log = LoggerFactory.getLogger(RestService.class);


    public RestService(final KafkaStreams streams, final HostInfo appServer, final HostInfo restServer) {
        this.streams = streams;
        this.metadataService = new MetadataService(streams);
        this.appServer = appServer;
        this.restServer = restServer;
    }

    /**
     * Get the rules for specific sensor
     * has the provided store.
     *
     * @param id The sensor id
     * @return Rules of {@link Map<String,Object>}
     */
    @GET()
    @Path("/query/rules/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public IotSensorRules queryRules(@PathParam("id") final String id) {
        IotSensorRules rules;

        // The data might be hosted on another instance. We need to find which instance it is on
        // and then perform a remote lookup if necessary.
        final HostStoreInfo
                host =
                metadataService.streamsMetadataForStoreAndKey(StreamBuilder.RULES_STORE_NAME, id, new
                        StringSerializer());

        // data is on another instance. call the other instance to fetch the data.
        if (!thisHost(host)) {
            String remoteHost = String.format("http://%s:%d/%s",
                    host.getHost(), host.getPort(), "iot-engine/query/" + StreamBuilder.RULES_STORE_NAME + "/" + id
            );
            log.info("Finding sensor {} rules, on instance {}", id, remoteHost);
            rules = client
                    .target(remoteHost)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<IotSensorRules>() {
                    });
        } else {
            log.info("Finding sensor {} rules, on local state", id);
            // look in the local store
            final ReadOnlyKeyValueStore<String, IotSensorRules> store = streams.store(StreamBuilder.RULES_STORE_NAME,
                    QueryableStoreTypes.keyValueStore());
            rules = store.get(id);
            if (rules == null) {
                rules = new IotSensorRules(id, Collections.EMPTY_LIST);
            }

        }

        return rules;
    }

    /**
     * Get the rules for specific sensor
     * has the provided store.
     *
     * @param id    The sensor id
     * @param start start time, example: 2007-12-03T10:15:30.00Z
     * @param end   end time, example: 2007-12-03T10:15:30.00Z
     * @return Rules of {@link Map<String,Object>}
     */
    @GET()
    @Path("/query/metrics/{id}/{start}/{end}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<IotDataMessage> queryMetrics(
            @PathParam("id") final String id,
            @PathParam("start") final String start,
            @PathParam("end") final String end
    ) {

        final HostStoreInfo
                host =
                metadataService.streamsMetadataForStoreAndKey(AGG_STORE_NAME, id, new
                        StringSerializer());

        List<IotDataMessage> result = new ArrayList<>();

        if (!thisHost(host)) {
            String remoteHost = String.format("http://%s:%d/%s",
                    host.getHost(),
                    host.getPort(),
                    "iot-engine/query/metrics/" + id + "/" + start + "/" + end
            );

            log.info("Finding sensor {} agg metrics, on instance {}", id, remoteHost);
            result.addAll(
                    client
                            .target(remoteHost)
                            .request(MediaType.APPLICATION_JSON_TYPE)
                            .get(new GenericType<List<IotDataMessage>>() {
                            })
            );
        } else {
            log.info("Finding sensor {} agg metrics, on local state", id);
            ReadOnlyWindowStore<String, IotDataAggregator> windowStore =
                    streams.store(AGG_STORE_NAME, QueryableStoreTypes.windowStore());

            Instant timeFrom = Instant.parse(start);
            Instant timeTo = Instant.parse(end);
            WindowStoreIterator<IotDataAggregator> iterator = windowStore.fetch(id, timeFrom, timeTo);
            while (iterator.hasNext()) {
                KeyValue<Long, IotDataAggregator> next = iterator.next();
                result.add(new IotDataMessage(next.key, id, next.value.generateAggMetrics()));
            }
            iterator.close();
        }

        return result;
    }

    /**
     * Get the metadata for all of the instances of this Kafka Streams application
     *
     * @return List of {@link HostStoreInfo}
     */
    @GET()
    @Path("/instances")
    @Produces(MediaType.APPLICATION_JSON)
    public List<HostStoreInfo> streamsMetadata() {
        return metadataService.streamsMetadata();
    }

    /**
     * Get the metadata for all instances of this Kafka Streams application that currently
     * has the provided store.
     *
     * @param store The store to locate
     * @return List of {@link HostStoreInfo}
     */
    @GET()
    @Path("/instances/{storeName}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<HostStoreInfo> streamsMetadataForStore(@PathParam("storeName") final String store) {
        return metadataService.streamsMetadataForStore(store);
    }

    private boolean thisHost(final HostStoreInfo host) {
        return host.getHost().equals(appServer.host()) &&
                host.getPort() == appServer.port();
    }

    /**
     * Start an embedded Jetty Server
     *
     * @throws Exception from jetty
     */
    public void start() throws Exception {
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        jettyServer = new Server();
        jettyServer.setHandler(context);

        final ResourceConfig rc = new ResourceConfig();
        rc.register(this);
        rc.register(JacksonFeature.class);

        final ServletContainer sc = new ServletContainer(rc);
        final ServletHolder holder = new ServletHolder(sc);
        context.addServlet(holder, "/*");

        final ServerConnector connector = new ServerConnector(jettyServer);
        connector.setHost(restServer.host());
        connector.setPort(restServer.port());
        jettyServer.addConnector(connector);

        context.start();

        try {
            jettyServer.start();
        } catch (final java.net.SocketException exception) {
            log.error("Unavailable: " + restServer.host() + ":" + restServer.port());
            throw new Exception(exception.toString());
        }
    }

    /**
     * Stop the Jetty Server
     *
     * @throws Exception from jetty
     */
    public void stop() throws Exception {
        if (jettyServer != null) {
            jettyServer.stop();
        }
    }

}

