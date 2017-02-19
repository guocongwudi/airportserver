package com.qantas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qantas.model.FeedResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableCaching
public class AirportServiceTest {

    @SpyBean
    private AirportService airportService;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    ObjectMapper objectMapper;


    @Value(value = "classpath:mockFeedData.json")
    private Resource mockedFeedDataResource;

    @Before
    public void setup() throws Exception {

        // airports will return from local json file in tests.
        String airportsJsonString = new BufferedReader(new InputStreamReader(mockedFeedDataResource.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        FeedResponse mockedFeedResponse = objectMapper.readValue(airportsJsonString, FeedResponse.class);
        doReturn(mockedFeedResponse.getAirports()).when(airportService).retrieveAllAirportsFromFeed();
    }

    @Test
    public void testCaching() throws Exception {

        // retrieve from feed will only be called once, as result cached
        this.airportService.retrieveAllAirports();
        verify(airportService, times(1)).retrieveAllAirports();
        verify(airportService,times(1)).retrieveAllAirportsFromFeed();
        this.airportService.retrieveAllAirports();
        verify(airportService, times(2)).retrieveAllAirports();
        verify(airportService, times(1)).retrieveAllAirportsFromFeed();

        // test filter caching, call same filter twice, the real function should only be executed only once.
        this.airportService.filterAirports("AAA",null,null,null,null,null,null);
        this.airportService.filterAirports("AAA",null,null,null,null,null,null);
        verify(airportService, times(1)).filterAirports("AAA",null,null,null,null,null,null);
    }
}


