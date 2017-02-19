package com.qantas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qantas.model.Airport;
import com.qantas.model.FeedResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by g on 17/02/2017.
 */

/**
 * service class to handle airport related operations
 */
@Service
public class AirportServiceImpl implements AirportService {
    @Value("${airport.data.feed.url}")
    String dataFeedUrl;
    @Value("${airport.api.default.pagesize}")
    String defaultPageSize;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CacheManager cacheManager;

    private static final Logger logger = LoggerFactory.getLogger(AirportServiceImpl.class);

    /**
     * filter airports base on provided params.cache the result from each search, expire every 3000s
     *
     * @param countryCode
     * @param airportCode
     * @param international
     * @param domestic
     * @param sortBy
     * @param pageNumber
     * @param pageSize
     * @return list of Airport
     * @throws IOException
     */
    @Override
     /*
     *  the airports list will only be scanned once in filtering and sorting. performance optimised
     * */
    public List<Airport> filterAirports(String countryCode, String airportCode, Boolean international, Boolean domestic, String sortBy, Integer pageNumber, Integer pageSize) throws IOException {
        List<Predicate<Airport>> filterCollection = new ArrayList<Predicate<Airport>>();
        List<Airport> airports = this.retrieveAllAirports();
        // sort by country code by default
        Comparator<Airport> defaultSortComparator = (a1, a2) -> a1.getCode().compareTo(a2.getCode());
        if (!StringUtils.isBlank(airportCode)) {
            filterCollection.add(a -> a.getCode().equals(airportCode.toUpperCase()));
        }
        if (!StringUtils.isBlank(countryCode)) {
            filterCollection.add(a -> a.getCountry().getCode().equals(countryCode.toUpperCase()));
        }
        if (domestic != null) {
            filterCollection.add(a -> a.getRegionalAirport() == domestic);
        }
        if (international != null) {
            filterCollection.add(a -> a.getInternationalAirport() == international);
        }
        if (!StringUtils.isBlank(sortBy)) {
            //default sort is by airport code
            if (sortBy.equals("country"))
            //sort by country code
            {
                defaultSortComparator = (a1, a2) -> a1.getCountry().getCode().compareTo(a2.getCountry().getCode());
            }
        }
        // combine all filters to a single predicate.
        Predicate<Airport> combinedFilterPredicate = filterCollection.stream().reduce(Predicate::and).orElse(x -> true);
        // apply all filters and materialize it to list
        airports = airports.stream().filter(combinedFilterPredicate).sorted(defaultSortComparator).collect(Collectors.toList());
        // handle pagination if set
        if (pageNumber != null || pageSize != null) {
            if (pageSize == null) {
                pageSize = Integer.valueOf(defaultPageSize);
            }
            int from = Math.max(0, (pageNumber - 1) * pageSize);
            int to = Math.min(airports.size(), (pageNumber) * pageSize);
            airports = airports.subList(from, to);
        }
        return airports;
    }

    /* */

    /**
     * retrieve  airport feed data from server, cache  the result from feed, expire every 3000s
     *
     * @return list of Airport
     * @throws IOException
     */
    @Override
    public List<Airport> retrieveAllAirports() throws IOException {
        List<Airport> airports = null;
        Cache.ValueWrapper airportsCache = cacheManager.getCache("airports").get("all");
        if (airportsCache != null) {
            return (List<Airport>) airportsCache.get();
        }
        logger.info("Retrieving airport data dump from feed.");
        airports = retrieveAllAirportsFromFeed();
        logger.info(airports.size() + " airports cached.");
        cacheManager.getCache("airports").putIfAbsent("all", airports);
        return airports;
    }

    /**
     * retrieve all airports from feed
     *
     * @return list of Airport
     * @throws IOException
     */
    @Override
    public List<Airport> retrieveAllAirportsFromFeed() throws IOException {
        String response = restTemplate.getForObject(dataFeedUrl, String.class);
        FeedResponse feedResponse = objectMapper.readValue(response, FeedResponse.class);
        return feedResponse.getAirports();
    }
}
