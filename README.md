# ChargeOptimizer
> Charge your car when electricity is cheapest, thereby helping the integration of renewable energy and stabilizing the power grid.

ChargeOptimizer communicates with your wallbox and controls the time at which your car is charged. Charging is shifted to times when the hourly electricity price is cheapest. It communicates via Modbus TCP with a Phoenix Contact charge controller (built into, e.g., Wallbe wallboxes).

## Features
- when you connect your car, ChargeOptimizer searches for the cheapest 3 hours within the next 8 hours (configurable) and charges your car only during these times
- electricity market prices are obtained via the ENTSOE transparency API (https://transparency.entsoe.eu/transmission-domain/r2/dayAheadPrices/show)
- can be controlled via a REST interface and connected to OpenHAB
- regularly logs its state into a database for analytics and statistics

## Getting Started
### Connecting your wallbox
Connect your wallbox to a computer via ethernet and configure its network settings. I recommend using a Raspberry Pi. In that case, you can configure a static IP address by adding these lines to `/etc/dhcpcd.conf`:
```
interface eth0
static ip_address=192.168.0.208/24
```

### Configuring ChargeOptimizer
ChargeOptimizer is configured via a configuration file. You can start with and adapt this example:
```
## Charger type and connection (Modbus TCP address)
charger = Wallbe
wallbeCharger.host = 192.168.0.8

## Uncomment the following two lines to use the ENTSOE transparency API for
## prices. Otherwise, an average price structure is used.
#costSource = EntsoeDayAhead
#entsoe.securityToken = PUT_YOUR_TOKEN_HERE

## This is the area EIC for Germany. For other codes, see https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html#_areas .
entsoe.areaCode = 10Y1001A1001A82H
entsoe.timezone = Europe/Berlin

## Uncomment these lines to store statistics to a H2 database:
#statisticsDatabase.url = jdbc:h2:/tmp/chargeoptim
#statisticsDatabase.user =
#statisticsDatabase.password =

## Uncomment the next line to run the web server. It will run without
## authentication, so only activate this in a protected network.
#webserver.port = 8081
```
Save this file as `chargeoptimizer.properties`.

### Running ChargeOptimizer
Download the latest release from https://github.com/t-pa/chargeoptimizer/releases and run it like this:
```
java -jar chargeoptimizer-x.y.z-jar-with-dependencies.jar chargeoptimizer.properties
```

## FAQ
- **Do I save money by charging when electricity is cheap?**  
  Only if you have a special electricity rate that changes during the day. These tariffs are still rare. However, the power system as a whole will benefit if more people shift their power usage to times when wind and solar energy are strong.
- **I have a different charger. How do I connect it?**  
  You can add support for a new type of charger by implementing the interface `Charger.java`. If your charger also has a Modbus TCP interface, it should be relatively easy to adapt `WallbeCharger.java`.

## License
This project is licensed under the GNU General Public License, version 3 or later. For details see [LICENSE.txt](./LICENSE.txt).