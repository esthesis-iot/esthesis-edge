# Enedis module

The esthesis EDGE Enedis module allows you to fetch data from the [Enedis DataHub API](https://datahub-enedis.fr).

The data the module supports includes:
- [Daily Consumption](https://datahub-enedis.fr/services-api/data-connect/documentation/metering-v5-consommation-quotidienne)
- [Daily Maximum Consumption](https://datahub-enedis.fr/services-api/data-connect/documentation/metering-v5-puissance-maximum-de-consommation)
- [Daily Production](https://datahub-enedis.fr/services-api/data-connect/documentation/metering-v5-production-quotidienne)

## Requirements
You need to have an Enedis application account, verified by Enedis, allowed to access the production Enedis DataHub API.

## User consent
In order for the esthesis EDGE to be able to fetch data from the Enedis DataHub API, the user must first give their 
consent following Enedis' procedures. The process starts with the user visiting an application page where a information
about the application as well as the purpose of the data collection is displayed. If the user agrees, they are redirected
to the Enedis login page where they can log in and give their consent. After the user has given their consent, they are
redirected back to the application where the consent is stored and the application can start fetching data.

The initial "welcome page" can be created by the Enedis module, featuring three custom images, a customised title, and
a customised message; it may look similar to this:

![enedis-welcome-page.png](enedis-welcome-page.png)

Alternatively, you can set up the Enedis module to redirect the user to a custom URL of your choice, where you can display
the information and ask for the user's consent. The URL can be set in the Enedis module configuration.

## Initial data fetch
When a new user is giving its consent to the Enedis module of esthesis EDGE, during the next data fetch cycle, 
the Enedis module will fetch the data for this user. How far back in time this initial data fetch goes can be configured
in the Enedis module configuration, with a default value of 30 days.

## Data types and adaptive error handling
As mentioned above, the Enedis module handles three types of data: daily consumption, daily maximum consumption, and daily
production. However, the Enedis DataHub API does not always return all three types of data as, for example, a user
might not produce electricity, or might not have provided consent for a specific data type. 
At the time being, the Enedis DataHub API does not provide an interface for third-parties
to discover what kind of data is available. For this reason, the esthesis EDGE Enedis module is operating on a 
"trial and error" basis, trying to fetch all three types of data and graciously handle any errors that might occur 
(i.e. an error while trying to fetch data type X does not prevent the module from fetching data type Y).

In order for the Enedis module to not keep trying to fetch data that is not available forever, each time an error occurs 
this is logged. Once the number of errors reaches a certain threshold, the Enedis module will stop trying to fetch this
specific type of data for this user. The threshold can be configured in the Enedis module configuration, and you should
tune it to your needs. Due to this nature of operation, you should expect to see some error messages in the logs, until
the Enedis module has discovered what kind of data is available for a specific user.

The Enedis module's administration API provides two endpoints allowing to get a list of errors and to reset the error 
count. 

## Configuration
For a full list of configuration options for the Enedis module, please refer to the [Enedis module configuration](EnedisConfiguration.md).