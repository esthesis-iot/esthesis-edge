# Enedis

The configuration for the esthesis EDGE Fronius module consists of the following parameters.

## General configuration

| **NAME**                                                                                         | **DESCRIPTION**                                                                                                                                      |
|--------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| **ESTHESIS_EDGE_MODULES_FRONIUS_ENABLED**<br/>--set esthesis.edge.modules.fronius.enabled        | Whether the Fronius module is enabled or not.<br/>Default: false                                                                                     |
| **ESTHESIS_EDGE_MODULES_FRONIUS_CRON**<br/>--set esthesis.edge.modules.fronius.cron              | A Quartz-type cron expression, specifying the frequency in which the Fronius module tries to fetch data from the Solar API.<br/>Default: 0 0 6 * * ? |
| **ESTHESIS_EDGE_MODULES_FRONIUS_MAX_DEVICES**<br/>--set esthesis.edge.modules.fronius.maxDevices | The total number of Fronius devices that can be registered in esthesis EDGE.<br/>Default: 1000                                                       |
| **QUARKUS_REST_CLIENT_FRONIUS_CLIENT**<br/>--set quarkus.restClient.FroniusClient                | The URL of the Fronius Solar API.<br/>Default: http://localhost:80/solar_api                                                            |

## Data fetching

| **NAME**                                                                                                                                            | **DESCRIPTION**                                                                                                           |
|-----------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| **ESTHESIS_EDGE_MODULES_FRONIUS_FETCH_TYPES_PFR_ENABLED**<br/>--set esthesis.edge.modules.fronius.fetchTypes.pfr.enabled                            | Enables fetching Power Flow real-time data.<br/>Default: false                                                            |
| **ESTHESIS_EDGE_MODULES_FRONIUS_FETCH_TYPES_PFR_INVERTERS_HARDWARE_IDS**<br/>--set esthesis.edge.modules.fronius.fetchTypes.pfr.invertersHadwareIds | The esthesis hardware ids, separated by comma, for the inverters in the PFR response.<br/>Default: fronius-pfr-inverter-0 |
| **ESTHESIS_EDGE_MODULES_FRONIUS_FETCH_TYPES_PRF_ERRORS_THRESHOLD**<br/>--set esthesis.edge.modules.fronius.fetchTypes.pfr.errorsThreshold           | The number of errors after which PFR fetching is disabled for a device.<br/>Default: 10                                   |
| **ESTHESIS_EDGE_MODULES_FRONIUS_FETCH_TYPES_PRF_EDAY_CATEGORY**<br/>--set esthesis.edge.modules.fronius.fetchTypes.pfr.eday.category                | Category name when synchronising PFR Daily data.<br/>Default: energy                                                      |
| **ESTHESIS_EDGE_MODULES_FRONIUS_FETCH_TYPES_PRF_EDAY_MEASUREMENT**<br/>--set esthesis.edge.modules.fronius.fetchTypes.pfr.eday.measurement          | Measurement name when synchronising PFR Daily data.<br/>Default: dailyProduction                                          |
