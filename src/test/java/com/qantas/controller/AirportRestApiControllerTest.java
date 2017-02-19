package com.qantas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qantas.model.FeedResponse;
import com.qantas.service.AirportServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@RunWith(SpringRunner.class)
@SpringBootTest
public class AirportRestApiControllerTest {

    static private MediaType JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private AirportServiceImpl airportService;


    @Value(value = "classpath:mockFeedData.json")
    private Resource mockedFeedDataResource;


    @Before
    public void setup() throws Exception {

        // setup mock mvc, airports will return from local json file in tests.
        mockMvc = webAppContextSetup(webApplicationContext).build();
        String airportsJsonString = new BufferedReader(new InputStreamReader(mockedFeedDataResource.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        FeedResponse mockedFeedResponse = objectMapper.readValue(airportsJsonString, FeedResponse.class);
        when(this.airportService.filterAirports(Matchers.<String>any(), Matchers.<String>any(), Matchers.<Boolean>any(), Matchers.<Boolean>any(), Matchers.<String>any(), Matchers.<Integer>any(), Matchers.<Integer>any())).thenCallRealMethod();
        when(this.airportService.retrieveAllAirports()).thenReturn(mockedFeedResponse.getAirports());
    }

    @Test
    public void testGetAirports() throws Exception {

        //test base api, briefly check the return size, and first element is the same as mockFeedData.json
        mockMvc.perform(get("/airports")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(50)))
                .andExpect(jsonPath("$[0].code", is("AAA")))
                .andExpect(jsonPath("$[0].display_name", is("Anaa")))
                .andExpect(jsonPath("$[0].international_airport", is(false)))
                .andExpect(jsonPath("$[0].regional_airport", is(false)))
                .andExpect(jsonPath("$[0].location.latitude", is(-17.416666)))
                .andExpect(jsonPath("$[0].location.longitude", is(-145.5)))
                .andExpect(jsonPath("$[0].currency_code", is("XPF")))
                .andExpect(jsonPath("$[0].timezone", is("Pacific/Tahiti")))
                .andExpect(jsonPath("$[0].country.code", is("PF")))
                .andExpect(jsonPath("$[0].country.display_name", is("French Polynesia")));

        //test basic filter functionality by airport code

        mockMvc.perform(get("/airports?airportcode=AAA")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is("AAA")));

        //test basic filter functionality by country code
        mockMvc.perform(get("/airports?countrycode=AU")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].country.code", everyItem(is("AU"))));

        //test basic filter functionality by domestic
        mockMvc.perform(get("/airports?domestic=true")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].regional_airport", everyItem(is(true))));

        //test basic filter functionality by international
        mockMvc.perform(get("/airports?international=false")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(50)))
                .andExpect(jsonPath("$[*].international_airport", everyItem(is(false))));

        //test basic sorting
        mockMvc.perform(get("/airports?sortby=country")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(50)))
                .andExpect(jsonPath("$[0].country.code", is("AE")))
                .andExpect(jsonPath("$[1].country.code", is("AU")));

        // test basic pagination
        mockMvc.perform(get("/airports?pagenumber=1&pagesize=10")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(10)));

        //test complex filter
        mockMvc.perform(get("/airports?pagenumber=1&pagesize=10&sortby=country&domestic=true&international=false&airportcode=ABX")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is("ABX")));
    }

    @Test
    public void testInvalidInput() throws Exception {

        //page size is invalid should return bad request with error code
        mockMvc.perform(get("/airports?pagenumber=1&pagesize=asd&sortby=country&domestic=true&international=false&airportcode=ABX")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$.code", is(2)));
    }
}
