/*
 *                                            Bosch SI Example Code License
 *                                              Version 1.0, January 2016
 *
 * Copyright 2017 Bosch Software Innovations GmbH ("Bosch SI"). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * BOSCH SI PROVIDES THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO
 * THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF
 * ALL NECESSARY SERVICING, REPAIR OR CORRECTION. THIS SHALL NOT APPLY TO MATERIAL DEFECTS AND DEFECTS OF TITLE WHICH
 * BOSCH SI HAS FRAUDULENTLY CONCEALED. APART FROM THE CASES STIPULATED ABOVE, BOSCH SI SHALL BE LIABLE WITHOUT
 * LIMITATION FOR INTENT OR GROSS NEGLIGENCE, FOR INJURIES TO LIFE, BODY OR HEALTH AND ACCORDING TO THE PROVISIONS OF
 * THE GERMAN PRODUCT LIABILITY ACT (PRODUKTHAFTUNGSGESETZ). THE SCOPE OF A GUARANTEE GRANTED BY BOSCH SI SHALL REMAIN
 * UNAFFECTED BY LIMITATIONS OF LIABILITY. IN ALL OTHER CASES, LIABILITY OF BOSCH SI IS EXCLUDED. THESE LIMITATIONS OF
 * LIABILITY ALSO APPLY IN REGARD TO THE FAULT OF VICARIOUS AGENTS OF BOSCH SI AND THE PERSONAL LIABILITY OF BOSCH SI'S
 * EMPLOYEES, REPRESENTATIVES AND ORGANS.
 */
package com.bosch.iot.things.examples.messages;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.client.live.LiveFeatureHandle;
import org.eclipse.ditto.client.live.LiveThingHandle;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bosch.iot.things.examples.common.ExamplesBase;
import com.bosch.iot.things.examples.common.model.ExampleUser;


/**
 * This examples shows the various possibilities that the {@code DittoClient} offers to register handlers for {@link
 * org.eclipse.ditto.model.messages.Message}s being sent to/from your {@code Thing}s, and shows how you can send such
 * {@code Message}s using the {@code DittoClient}. NOTE: Make sure to invoke {@code
 * ThingsClient.twin().startConsumption()} once after all message handlers are registered to start receiving events.
 */
public final class RegisterForAndSendMessages extends ExamplesBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterForAndSendMessages.class);

    private static final String ALL_THINGS_JSON_MESSAGE = "allThings_jsonMessage";
    private static final String ALL_THINGS_RAW_MESSAGE = "allThings_rawMessage";
    private static final String ALL_THINGS_STRING_MESSAGE = "allThings_stringMessage";
    private static final String MY_THING_JSON_MESSAGE = "myThing_jsonMessage";
    private static final String MY_THING_RAW_MESSAGE = "myThing_rawMessage";
    private static final String MY_THING_STRING_MESSAGE = "myThing_stringMessage";
    private static final String CUSTOM_SERIALIZER_EXAMPLE_USER_MESSAGE = "customSerializer_exampleUserMessage";
    private final CountDownLatch countDownLatch;
    private final String fromThingId;
    private final String toThingId;

    public RegisterForAndSendMessages() throws InterruptedException, ExecutionException, TimeoutException {

        fromThingId = generateRandomThingId("fromThingId_");
        LOGGER.info("Creating thing {} as message source.", fromThingId);
        client.twin().create(ThingId.of(fromThingId))
                .thenCompose(created -> {
                    final Thing updated = created.toBuilder()
                            .build();
                    return client.twin().update(updated);
                }).get(10, TimeUnit.SECONDS);

        toThingId = generateRandomThingId("toThingId_");
        LOGGER.info("Creating thing {} as message sink.", toThingId);
        client.twin().create(ThingId.of(toThingId))
                .thenCompose(created -> {
                    final Thing updated = created.toBuilder()
                            .build();
                    return client.twin().update(updated);
                }).get(10, TimeUnit.SECONDS);

        client.live().startConsumption().get(10, TimeUnit.SECONDS);
        client2.live().startConsumption().get(10, TimeUnit.SECONDS);

        countDownLatch = new CountDownLatch(17);
    }

    public static void main(final String... args) throws Exception {
        final RegisterForAndSendMessages registerForAndSendMessages = new RegisterForAndSendMessages();
        try {
            registerForAndSendMessages.registerForMessages();
            registerForAndSendMessages.sendMessages();
        } finally {
            registerForAndSendMessages.destroy();
        }
    }

    /**
     * Shows various possibilities to register handlers for {@code Message}s of interest.
     */
    public void registerForMessages() {
        /* Register for *all* messages of *all* things and provide payload as String */ /**/
        client.live().registerForMessage(ALL_THINGS_STRING_MESSAGE, "*", String.class, message -> {
            String subject = message.getSubject();
            Optional<String> payload = message.getPayload();
            LOGGER.info("Match all String Messages: message for subject {} with payload {} received", subject, payload);
            countDownLatch.countDown();
        });

        /* Register for *all* messages with subject *jsonMessage* of *all* things and provide payload as JsonValue */
        client.live().registerForMessage(ALL_THINGS_JSON_MESSAGE, "jsonMessage", JsonValue.class, message -> {
            String subject = message.getSubject();
            Optional<JsonValue> payload = message.getPayload();
            LOGGER.info("Match Json Message: message for subject {} with payload {} received", subject, payload);
            countDownLatch.countDown();
        });

        /* Register for messages with subject *rawMessage* of *all* things and provide payload as raw ByteBuffer */
        client.live().registerForMessage(ALL_THINGS_RAW_MESSAGE, "rawMessage", message -> {
            String subject = message.getSubject();
            Optional<ByteBuffer> payload = message.getRawPayload();
            final String payloadAsString = payload.map(p -> StandardCharsets.UTF_8.decode(p).toString()).orElse(null);
            LOGGER.info("Match Raw Message: message for subject {} with payload {} received", subject,
                    payloadAsString);
            countDownLatch.countDown();
        });


        final LiveThingHandle fromThingHandle = client.live().forId(ThingId.of(fromThingId));

        /* Register for *all* messages of a *specific* thing and provide payload as String */
        fromThingHandle.registerForMessage(MY_THING_STRING_MESSAGE, "*", String.class, message -> {
            String subject = message.getSubject();
            Optional<String> payload = message.getPayload();
            LOGGER
                    .info("Match all String Messages for fromThingId: message for subject {} with payload {} received",
                            subject,
                            payload);
            countDownLatch.countDown();
        });

        /* Register for *all* messages with subject *myThingJsonMessage* of a *specific* thing of and provide payload as JsonValue */
        /* not used */
        fromThingHandle.registerForMessage(MY_THING_JSON_MESSAGE, "jsonMessage", JsonValue.class, message -> {
            String subject = message.getSubject();
            Optional<JsonValue> payload = message.getPayload();
            LOGGER.info("Match Json Messages for fromThingId: message for subject {} with payload {} received", subject,
                    payload);
            countDownLatch.countDown();
        });

        /* Register for *all* messages with subject *myThingRawMessage* of a *specific* thing and provide payload as raw ByteBuffer */
        /* not used */
        fromThingHandle.registerForMessage(MY_THING_RAW_MESSAGE, "rawMessage", message -> {
            String subject = message.getSubject();
            Optional<ByteBuffer> payload = message.getRawPayload();
            final String payloadAsString = payload.map(p -> StandardCharsets.UTF_8.decode(p).toString()).orElse(null);
            LOGGER.info("Match Raw Messages for fromThingId: message for subject {} with payload {} received", subject,
                    payloadAsString);
            countDownLatch.countDown();
        });

        /*
         * Custom Message serializer usage:
         */

        /* Register for messages with subject *example.user.created* of *all* things and provide payload as custom type ExampleUser */
        client.live()
                .registerForMessage(CUSTOM_SERIALIZER_EXAMPLE_USER_MESSAGE, "example.user.created", ExampleUser.class,
                        message -> {
                            String subject = message.getSubject();
                            Optional<ExampleUser> user = message.getPayload();
                            LOGGER.info("Match Custom Message: message for subject {} with payload {} received",
                                    subject, user);
                            countDownLatch.countDown();
                        });

    }

    /**
     * Shows how to send a {@code Message} to/from a {@code Thing} using the {@code ThingsClient}.
     */
    public void sendMessages() {
        /* Send a message *from* a thing with the given subject but without any payload */
        client2.live().message()
                .from(ThingId.of(fromThingId))
                .subject("some.arbitrary.subject")
                .send();

        /* Send a message *from* a feature with the given subject but without any payload */
        //does not arrive
        client2.live().message()
                .from(ThingId.of(fromThingId))
                .featureId("sendFromThisFeature")
                .subject("justWantToLetYouKnow")
                .send();

        /* Send a message *to* a thing with the given subject and text payload */
        /* We won't receive this message because we send it to another Thing Client.*/
        client2.live().message()
                .to(ThingId.of(toThingId))
                .subject("monitoring.building.fireAlert")
                .payload("Roof is on fire")
                .contentType("text/plain")
                .send();

        /* Send a message *from* a feature with the given subject and json payload */
        client2.live().message()
                .from(ThingId.of(toThingId))
                .featureId("smokeDetector")
                .subject("jsonMessage")
                .payload(JsonFactory.readFrom("{\"action\" : \"call fire department\"}"))
                .contentType("application/json")
                .send();

        /* Send a message *to* a feature with the given subject and raw payload */
        client2.live().message()
                .from(ThingId.of(fromThingId))
                .featureId("smokeDetector")
                .subject("rawMessage")
                .payload(ByteBuffer.wrap("foo".getBytes(StandardCharsets.UTF_8)))
                .contentType("application/octet-stream")
                .send();

        final LiveThingHandle thingHandle = client2.live().forId(ThingId.of(toThingId));
        /* Send a message *to* a thing (id already defined by the ThingHandle) with the given subject but without any payload */
        thingHandle.message()
                .to()
                .subject("somesubject")
                .send();

        final LiveFeatureHandle featureHandle = client2.live().forFeature(ThingId.of(fromThingId), "smokeDetector");
        /* Send a message *from* a feature with the given subject and text payload */
        featureHandle.message()
                .from()
                .subject("somesubject")
                .payload("someContent")
                .contentType("text/plain")
                .send();

        /*
         * Custom Message serializer usage:
         */
        /* Send a message *from* a thing with the given subject and a custom payload type */
        client2.live()
                .message()
                .from(ThingId.of(fromThingId))
                .subject("example.user.created")
                .payload(new ExampleUser("karl", "karl@bosch.com"))
                .contentType(ExampleUser.USER_CUSTOM_CONTENT_TYPE)
                .send();
    }

    public void destroy() throws InterruptedException {
        boolean allMessagesReceived = countDownLatch.await(10, TimeUnit.SECONDS);
        LOGGER.info("All messages received: {}", allMessagesReceived);
        terminate();
    }
}
