FROM openjdk:8-jre-alpine

ENV VERTICLE_FILE Duplicati-Monitoring-0.1-SNAPSHOT-all.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles
ENV LISTEN_PORT 8080
ENV MQTT_HOST openhab-s1

EXPOSE $LISTEN_PORT

# Copy your fat jar to the container
COPY build/libs/$VERTICLE_FILE $VERTICLE_HOME/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -DLISTEN_PORT=$LISTEN_PORT -DMQTT_HOST=$MQTT_HOST -jar $VERTICLE_FILE "]