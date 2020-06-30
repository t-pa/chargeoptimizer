/*
 * Copyright (C) 2020 t-pa <t-pa@posteo.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package chargeoptimizer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supplies day-ahead price information from the ENTSOE transparency API. The
 * data is cached.
 */
public class EntsoeDayAhead implements CostSource {
    
    final Logger logger = LoggerFactory.getLogger(EntsoeDayAhead.class);
    
    private final TreeMap<LocalDateTime, Double> prices = new TreeMap<>();
    private final String areaEIC;
    private final ZoneId timezone;
    private final String securityToken;
    private int maxCacheSize = 5000;
    
    // if the server answered BAD REQUEST, for some time do not try again the same request or a
    // request for an even earlier time
    private LocalDateTime unavailableDay = null;
    private LocalDateTime wasUnavailableAt = LocalDateTime.MIN;
    private Duration coolOffTime = Duration.ofMinutes(5);
    
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    
    /**
     * Day-ahead price information for a certain area.
     * A list of valid area EIC codes can be found at
     * <a href="https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html#_areas">
     * https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html#_areas</a>.
     * The time zone is necessary to calculate the local start and end of day.
     * The ENTSOE transparency API is protected by a security token which you
     * can get by registering at <a href="https://transparency.entsoe.eu/">
     * https://transparency.entsoe.eu/</a> and then writing to
     * <a href="mailto:transparency@entsoe.eu">transparency@entsoe.eu</a>.
     * @param areaEIC 
     * @param timezone
     * @param securityToken
     */
    public EntsoeDayAhead(String areaEIC, ZoneId timezone, String securityToken) {
        this.areaEIC = areaEIC;
        this.timezone = timezone;
        this.securityToken = securityToken;
        
        logger.info("areaEIC = " + areaEIC + ", timezone = " + timezone);
    }
    
    @Override
    public Double getCostAt(LocalDateTime time) {
        LocalDateTime fullHour = time.truncatedTo(ChronoUnit.HOURS);
        Double price = prices.get(fullHour);
        if (price != null) {
            return price;
        } else {
            // if price is not in cache, get whole day
            fetchCostsAt(fullHour);
            return prices.get(fullHour);  // this might still be null
        }
    }

    /** Fetches the energy prices for the whole day containing the specified
     * time and stores them in prices.
     * @param time 
     */
    private void fetchCostsAt(LocalDateTime time) {
        if (prices.size() > getMaxCacheSize()-24) prices.clear();
        
        // calculate start and end of local-time day in UTC
        final ZonedDateTime timeLocal = time.atZone(TimeUtils.UTC).withZoneSameInstant(timezone);
        final ZonedDateTime dayStartLocal = timeLocal.truncatedTo(ChronoUnit.DAYS);
        final ZonedDateTime dayEndLocal = dayStartLocal.plus(1, ChronoUnit.DAYS);
        
        final LocalDateTime dayStart = dayStartLocal.withZoneSameInstant(TimeUtils.UTC).toLocalDateTime();
        final LocalDateTime dayEnd = dayEndLocal.withZoneSameInstant(TimeUtils.UTC).toLocalDateTime();
        
        // field off requests too far in the future for day-ahead prices
        if (time.isAfter(TimeUtils.now().plusDays(2))) {
            logger.debug("Ignored request too far in the future.");
            return;
        }
        // if a similar request has failed recently, do not try again now
        if (unavailableDay != null && unavailableDay.compareTo(dayStart) <= 0)
            if (TimeUtils.now().isBefore(wasUnavailableAt.plus(coolOffTime)))
                return;

        fetchCosts(dayStart, dayEnd);
    }
    
    protected void fetchCosts(LocalDateTime start, LocalDateTime end) {
        logger.info("Fetching data from " + start + " to " + end + ".");
        
        try {
            URL url = new URL("https://transparency.entsoe.eu/api?securityToken=" +
                    securityToken + "&documentType=A44" +
                    "&in_Domain=" + areaEIC + "&out_Domain=" + areaEIC +
                    "&periodStart=" + start.format(DATE_FORMAT) +
                    "&periodEnd=" + end.format(DATE_FORMAT));

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            if (conn.getResponseCode() == 400) {
                logger.warn("Server returned 400 BAD REQUEST.");
                // cache error response for a certain time
                unavailableDay = start;
                wasUnavailableAt = TimeUtils.now();
            } else {
                try (InputStream input = conn.getInputStream()) {
                    loadXML(input);
                }
            }
        } catch (IOException | XMLStreamException  ex) {
            logger.error("Could not fetch data.", ex);
        }        
    }
    
    private void loadXML(InputStream source) throws XMLStreamException {
        XMLInputFactory xif = XMLInputFactory.newFactory();
        XMLEventReader reader = xif.createXMLEventReader(source);
        
        String currentElement = "";
        LocalDateTime startDate = null;
        LocalDateTime timestamp = null;

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement el = event.asStartElement();
                    currentElement = el.getName().getLocalPart(); 
                    break;
                    
                case XMLStreamConstants.END_ELEMENT:
                    currentElement = "";
                    break;
                    
                case XMLStreamConstants.CHARACTERS:
                    String data = event.asCharacters().getData();
                    switch (currentElement) {
                        case "start":
                            // remove trailing Z:
                            if (data.endsWith("Z"))
                                data = data.substring(0, data.length()-1);
                            startDate = LocalDateTime.parse(data);
                            break;
                        case "position":
                            if (startDate != null)
                                timestamp = startDate.plusHours(Integer.parseInt(data)-1);
                            break;
                        case "price.amount":
                            prices.put(timestamp, Double.parseDouble(data));
                            break;
                    }
                    break;
            }
        }
        reader.close();
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        if (prices.size() > maxCacheSize) prices.clear();
    }
    
    protected TreeMap<LocalDateTime, Double> getPrices() {
        return new TreeMap<>(prices);
    }
    
}