package com.synchronicity.APBdev.connectivity;

/**
 * Created by Andrew on 2018-03-16.
 */

/*
    The class NsdManager defines an interface for objects which drive Network Service Discovery.
    The class uses a wrapper called NsdServiceInfo to polymorphically wrap any kind of type that the
    client wishes to use to identify information about their service. This wrapper uses Java generic
    templates to fulfil this purpose, and so it is incumbant upon the client to know exaclty what
    types that they will be using in the implementation of NsdManager.
 */

public interface NsdManager {
        /*
            The client should use this method to advertise all of the information that they wish that
            other to know so that they may use the service. The information about the service is
            contained in the wrapper class NsdServiceInfo.
         */
        void advertiseService(NsdServiceInfo info);
        /*
            The client should use this method to find service that are available to be connected to
            and use the NsdServiceInfo provided to filter only the services that they would like to
            connect to, discarding the rest.
         */
        void findService(NsdServiceInfo info);
        /*
            The client should use this method to connect to one of the services which has found.
            They provid an NsdServiceInfo object to help identify which of the found services that
            they would like to connect to.
         */
        void connectToService(NsdServiceInfo info);

}