package com.synchronicity.APBdev.connectivity;

/**
 * Created by Andrew on 2018-03-16.
 */

/*
    The class NsdManager defines an interface for objects which drive Network Service Discovery.
    The class uses a wrapper called NsdInfo to polymorphically wrap any kind of type that the
    client wishes to use to identify information about their service. This wrapper uses Java generic
    templates to fulfil this purpose, and so it is incumbant upon the client to know exaclty what
    types that they will be using in the implementation of NsdManager.
 */

public interface NsdManager {

        /*
            The client should use this method to advertise all of the information that they wish that
            other to know so that they may use the service. The information about the service is
            contained in the wrapper class NsdInfo.
         */
        void advertiseService(NsdInfo info);

        /*
            The client should use this method to find service that are available to be connected to
            and use the NsdInfo provided to filter only the services that they would like to
            connect to, discarding the rest.
         */
        void findService(NsdInfo info);

        /*
            The client should use this method to connect to one of the services which has found.
            They provid an NsdInfo object to help identify which of the found services that
            they would like to connect to.
         */
        void connectToService(NsdInfo info);

        /*
            The client should use this method if there are any resources that need to be cleaned up
            in the class that implements the interface.
         */
        void cleanUp();

}