package com.qantas.service;



import com.qantas.model.Airport;
import org.springframework.cache.annotation.Cacheable;

import java.io.IOException;
import java.util.List;

/**
 * Created by g on 17/02/2017.
 */

public interface AirportService {

    /**
     * filter airport
     * @param countryCode
     * @param airportCode
     * @param international
     * @param domestic
     * @param sortBy
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws IOException
     */
    @Cacheable("airportSearchResult")
    List<Airport> filterAirports(String countryCode, String airportCode, Boolean international, Boolean domestic, String sortBy, Integer pageNumber, Integer pageSize) throws IOException;

    /**
     * retrieve all airport
     * @return
     * @throws IOException
     */
    List<Airport> retrieveAllAirports() throws IOException;

    /**
     * call external feed to retrieve all airports
     * @return
     * @throws IOException
     */
    List<Airport> retrieveAllAirportsFromFeed() throws IOException;
}

