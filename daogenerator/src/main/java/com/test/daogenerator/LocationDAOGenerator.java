package com.test.daogenerator;


import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class LocationDAOGenerator{
public static void main(String [] args) throws Exception{
    Schema schema = new Schema(1,"mobilecomputing.hsalbsig.de.mylocation.dao");
    Entity marker = schema.addEntity("Marker");
    marker.addIdProperty();
    marker.addDoubleProperty("Latitude");
    marker.addDoubleProperty("Longitude");
    marker.addStringProperty("Text");
    Property property = marker.addLongProperty("MarkerId").getProperty();

    Entity track = schema.addEntity("Track");
    Property trackProperty = track.addIdProperty().getProperty();
    track.addStringProperty("Name");
    track.addDateProperty("LogTime");
    ToMany manytracks = track.addToMany(marker, property);
    marker.addToOne(track, property);
    manytracks.setName("Track");
    new DaoGenerator().generateAll(schema,"E:\\Eigene Dateien\\Desktop\\Android\\MyLocation\\daogenerator\\marker\\src-gen");
}
}