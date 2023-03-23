package com.example.demo;

import com.google.protobuf.Message;
//import com.google.transit.realtime.GtfsRealtimeConstants;
import lombok.extern.java.Log;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import com.google.transit.realtime.GtfsRealtime.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.onebusaway.gtfs.model.*;

@Log

public class GTFSRealtime {
	
    //@Value("${zip-to-pbf.dest}")
    private String outputPath;

    // reads a GTFS zip and converts it to a GTFS-RT
    
    @Handler
    @GetMapping("/read")
    public void perform(Exchange exchange) throws Exception {
        try {
           File srcFile = exchange.getIn().getBody(File.class);

            exchange.getIn().setHeader(Exchange.FILE_PATH, srcFile.getAbsolutePath());

            LocalDate date = LocalDate.now(ZoneId.of("Asia/Hong_Kong"));


            GtfsReader reader = new GtfsReader();
            reader.setInputLocation(srcFile);
//            System.out.println("This is not working");
//            GtfsReader reader = new GtfsReader(); reader.setInputLocation(new
//            		  File("src/main/resources/pl_gtfs"));

            reader.run();



        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    @GetMapping("/tripupdate")
    
    public static void tripUpdate( StopTime stopTime, int delay, String outputPath ) throws Exception
    {
        File fullpath = new File(outputPath);
        internal_trip_update(stopTime,delay, fullpath);
    	

    }
    @GetMapping("/header")
    private static FeedHeader.Builder createFeedHeader(long timestamp) {
        FeedHeader.Builder header = FeedHeader.newBuilder();
        header.setTimestamp(timestamp);
        header.setIncrementality(FeedHeader.Incrementality.DIFFERENTIAL);
        //header.setGtfsRealtimeVersion(GtfsRealtimeConstants.VERSION);
        header.setGtfsRealtimeVersion("2.0");
        return header;
    }

    // create a Trip Update message
    @GetMapping("/internal")
    private static void internal_trip_update(StopTime stopTime, int delay, File outputpath) throws Exception {
        
        FeedMessage.Builder feedMessageBuilder = FeedMessage.newBuilder();

        FeedHeader.Builder header = createFeedHeader(System.currentTimeMillis());

        feedMessageBuilder.setHeader(header);

        TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();
        TripUpdate.StopTimeUpdate.Builder stopTimeUpdate = TripUpdate.StopTimeUpdate.newBuilder();
        TripUpdate.StopTimeEvent event1 = TripUpdate.StopTimeEvent.newBuilder().setDelay(delay).build();


        stopTimeUpdate.setStopId(stopTime.getId().toString());
        stopTimeUpdate.setStopSequence(stopTime.getStopSequence());
        stopTimeUpdate.setArrival(event1);

        tripUpdate.addStopTimeUpdate(stopTimeUpdate);

        TripDescriptor descriptor = TripDescriptor.newBuilder().setTripId(stopTime.getTrip().getId().getId())
                .setRouteId(stopTime.getTrip().getRoute().getId().getId()).build();

        tripUpdate.setTrip(descriptor);

             //   TripDescriptor.ScheduleRelationship.CANCELED);

        FeedEntity.Builder entity = FeedEntity.newBuilder();
        entity.setId("entityId");
        entity.setTripUpdate(tripUpdate);
        feedMessageBuilder.addEntity(entity);

        FeedMessage message = feedMessageBuilder.build();

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputpath));
        message.writeTo(out);
        out.close();

    }

    private void vehicle() throws Exception {

        FeedMessage.Builder feedMessageBuilder = FeedMessage.newBuilder();

        FeedHeader.Builder header = FeedHeader.newBuilder();
        header.setTimestamp(System.currentTimeMillis());
        header.setIncrementality(FeedHeader.Incrementality.DIFFERENTIAL);
        //header.setGtfsRealtimeVersion(GtfsRealtimeConstants.VERSION);
        header.setGtfsRealtimeVersion("2.0");

        feedMessageBuilder.setHeader(header);

        VehiclePosition.Builder vp = VehiclePosition.newBuilder();

        TripDescriptor.Builder td = TripDescriptor.newBuilder();
        td.setTripId("tripId");
        vp.setTrip(td);

        VehicleDescriptor.Builder vd = VehicleDescriptor.newBuilder();
        vd.setId("vehicleId");
        vp.setVehicle(vd);

        vp.setTimestamp(System.currentTimeMillis());

        Position.Builder position = Position.newBuilder();
        position.setLatitude((float) 47.653738);
        position.setLongitude((float) -122.307786);
        vp.setPosition(position);

        FeedEntity.Builder entity = FeedEntity.newBuilder();
        entity.setId("entityId");
        entity.setVehicle(vp);
        feedMessageBuilder.addEntity(entity);

        FeedMessage message = feedMessageBuilder.build();

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("path/to/output"));
        message.writeTo(out);
        out.close();
    }

}
