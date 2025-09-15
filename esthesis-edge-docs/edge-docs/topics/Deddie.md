# Deddie module

The esthesis EDGE DEDDIE module allows you to fetch data from the [DEDDIE Metering API](https://apps.deddie.gr/rps/swagger/index.html?swagger_url=https://apps.deddie.gr/mdp/rest/swagger.json).

The module supports fetching 15-minute interval curves for the four available class types:
- Active
- Reactive
- Produced
- Injected

## Requirements
To access data from the DEDDIE API, the following credentials are required:
- Tax number / VAT number of the bill recipient
- 9-digit electricity supply number (found on the electricity bill)
- Access token, issued via the [DEDDIE portal](https://apps.deddie.gr/mdp/mdpAccessTokens.html)

## Self-registration and token renewal
For esthesis EDGE to fetch data from the DEDDIE Metering API, users must first register their DEDDIE credentials themselves.

The process begins when the user visits the self-registration page, available at the `/deddie/public/self-registration` endpoint, 
and fills out a form with the required credentials (Tax number, Access token, and Supply numbers).
After submitting the form, the credentials are stored, and the application can begin fetching data.

This self-registration page can be customized with three images, a custom title, a message, and placeholder text for each 
credential input:

![deddie-self-registration-page.png](deddie-self-registration-page.png)

Since the Tax number is the primary key for identifying a user in the DEDDIE API, this page also serves as the entry point 
for updating an access token once it expires. 

When a user submits the form, the provided data is validated against the DEDDIE API. If validation succeeds, the setup is complete,
and during the next data-fetch cycle, the user’s data will be retrieved.

## Data types 
As mentioned, the DEDDIE module handles four types of data, which we name:
- Curve Active Consumption (CAC)
- Curve Reactive Power (CRP)
- Curve Energy Produced (CEP)
- Curve Energy Injected (CEI)

All data is retrieved in 15-minute intervals, which is the most granular resolution provided by the DEDDIE API.

## Configuration

The configuration parameters of the esthesis EDGE DEDDIE module allow you to control the following:
- Whether the module is enabled or disabled
- Which data types to fetch
- How far back in time the initial data fetch should go
- How often should data be fetched
- How many retries should be attempted in case of errors before giving up on a specific type

### General configuration

| **NAME**                                                                                            | **DESCRIPTION**                                                                                                                                      |
|-----------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| **ESTHESIS_EDGE_MODULES_DEDDIE_ENABLED**<br/>--set esthesis.edge.modules.deddie.enabled             | Whether the DEDDIE module is enabled or not.<br/>Default: false                                                                                      |
| **ESTHESIS_EDGE_MODULES_DEDDIE_CRON**<br/>--set esthesis.edge.modules.deddie.cron                   | A Quartz-type cron expression, specifying the frequency in which the DEDDIE module tries to fetch data from the DEDDIE API.<br/>Default: 0 0 6 * * ? |
| **ESTHESIS_EDGE_MODULES_DEDDIE_MAX_DEVICES**<br/>--set esthesis.edge.modules.deddie.maxDevices      | The total number of DEDDIE devices that can be registered in esthesis EDGE.<br/>Default: 1000                                                        |
| **ESTHESIS_EDGE_MODULES_DEDDIE_PAST_DAYS_INIT**<br/>--set esthesis.edge.modules.deddie.pastDaysInit | The number of days in the past to fetch data from the DEDDIE API for a newly registered device.<br/>Default: 7                                       |
| **QUARKUS_REST_CLIENT_DEDDIE_CLIENT_URL**<br/>--set quarkus.restClient.DeddieClient.url             | The URL of the DEDDIE API.<br/>Default: https://apps.deddie.gr/mdp/rest                                                                              |

### Data fetching

| **NAME**                                                                                                                                | **DESCRIPTION**                                                                         |
|-----------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CAC_ENABLED**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cac.enabled                  | Enables fetching Curve Energy Consumption (CAC) data.<br/>Default: true                 |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CAC_CATEGORY**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cac.category                | Category name when synchronising CAC data.<br/>Default: energy                          |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CAC_MEASUREMENT**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cac.measurement          | Measurement name when synchronising CAC data.<br/>Default: active                       |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CAC_ERRORS_THRESHOLD**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cac.errorsThreshold | The number of errors after which CAC fetching is disabled for a device.<br/>Default: 10 |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CRP_ENABLED**<br/>--set esthesis.edge.modules.deddie.fetchTypes.crp.enabled                  | Enables fetching Curve Reactive Power (CRP) data.<br/>Default: true                     |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CRP_CATEGORY**<br/>--set esthesis.edge.modules.deddie.fetchTypes.crp.category                | Category name when synchronising CRP data.<br/>Default: energy                          |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CRP_MEASUREMENT**<br/>--set esthesis.edge.modules.deddie.fetchTypes.crp.measurement          | Measurement name when synchronising CRP data.<br/>Default: reactive                     |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CRP_ERRORS_THRESHOLD**<br/>--set esthesis.edge.modules.deddie.fetchTypes.crp.errorsThreshold | The number of errors after which CRP fetching is disabled for a device.<br/>Default: 10 |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CEP_ENABLED**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cep.enabled                  | Enables fetching Curve Energy Produced (CEP) data.<br/>Default: true                    |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CEP_CATEGORY**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cep.category                | Category name when synchronising CEP data.<br/>Default: energy                          |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CEP_MEASUREMENT**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cep.measurement          | Measurement name when synchronising CEP data.<br/>Default: produced                     |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CEP_ERRORS_THRESHOLD**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cep.errorsThreshold | The number of errors after which CEP fetching is disabled for a device.<br/>Default: 10 |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CEI_ENABLED**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cei.enabled                  | Enables fetching Curve Energy Injected (CEI) data.<br/>Default: true                    |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CEI_CATEGORY**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cei.category                | Category name when synchronising CEI data.<br/>Default: energy                          |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CEI_MEASUREMENT**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cei.measurement          | Measurement name when synchronising CEI data.<br/>Default: injected                     |
| **ESTHESIS_EDGE_MODULES_DEDDIE_FETCH_TYPES_CEI_ERRORS_THRESHOLD**<br/>--set esthesis.edge.modules.deddie.fetchTypes.cei.errorsThreshold | The number of errors after which CEI fetching is disabled for a device.<br/>Default: 10 |

### Self-registration

| **NAME**                                                                                                                                                                                         | **DESCRIPTION**                                                                                                                                                                                                                                                                                           |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_ENABLED**<br/>--set esthesis.edge.modules.deddie.selfRegistration.enabled                                                                       | Enables the self-registration page for DEDDIE's end-users.<br/>Default: true                                                                                                                                                                                                                              |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_LOGO1_URL**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.logo1Url                                                          | The URL of the first logo to be displayed in the self-registration page.<br/>Default: https://deddie.gr/images/svgs/deddie-el.svg                                                                                                                                                                         |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_LOGO1_ALT**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.logo1Alt                                                          | The alt text of the first logo to be displayed in the self-registration page.<br/>Default: Deddie                                                                                                                                                                                                         |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_LOGO2_URL**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.logo2Url                                                          | The URL of the second logo to be displayed in the self-registration page.<br/>Default: https://www.eurodyn.com/wp-content/uploads/2018/11/logo_ed.png                                                                                                                                                     |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_LOGO2_ALT**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.logo2Alt                                                          | The alt text of the second logo to be displayed in the self-registration page.<br/>Default: European Dynamics                                                                                                                                                                                             |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_LOGO3_URL**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.logo3Url                                                          | The URL of the second logo to be displayed in the self-registration page.<br/>Default: https://esthes.is/docs/edge/images/logo.png                                                                                                                                                                        |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_LOGO3_ALT**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.logo3Alt                                                          | The alt text of the third logo to be displayed in the self-registration page.<br/>Default: esthesis EDGE                                                                                                                                                                                                  |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_REGISTRATION_TITLE**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.registration.title                                       | The title of the self-registration page.<br/>Default: DEDDIE Data Registration                                                                                                                                                                                                                            |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_REGISTRATION_MESSAGE**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.registration.message                                   | The text of the self-registration page.<br/>Default: Please provide your Tax Number and the Access Token obtained from DEDDIE's Metering Data Portal. Optionally, you may enter one or more Supply Numbers. If no Supply Numbers are provided, all supplies linked to your Tax Number will be retrieved.  |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_REGISTRATION_PLACEHOLDER_TAX_NUMBER**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.registration.placeholderTaxNumber       | The placeholder text of the tax number input in the self-registration page.<br/>Default: Tax Number (required).                                                                                                                                                                                           |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_REGISTRATION_PLACEHOLDER_ACCESS_TOKEN**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.registration.placeholderAccessToken   | The placeholder text of the access token input in the self-registration page.<br/>Default: Access Token (required).                                                                                                                                                                                       |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_REGISTRATION_PLACEHOLDER_SUPPLY_NUMBER**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.registration.placeholderSupplyNumber | The placeholder text of the supply number input in the self-registration page.<br/>Default: Supply Number (optional).                                                                                                                                                                                     |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_SUCCESS_TITLE**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.success.title                                                 | The title of the successful registration page.<br/>Default: Registration Successful                                                                                                                                                                                                                       |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_SUCCESS_MESSAGE**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.success.message                                             | The text of the successful registration page.<br/>Default: Your information has been registered successfully. You may now close this window.                                                                                                                                                              |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_ERROR_TITLE**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.error.title                                                     | The title of the unsuccessful registration page.<br/>Default: Registration Failed                                                                                                                                                                                                                         |
| **ESTHESIS_EDGE_MODULES_DEDDIE_SELF_REGISTRATION_PAGE_ERROR_MESSAGE**<br/>--set esthesis.edge.modules.deddie.selfRegistration.page.error.message                                                 | The text of the unsuccessful registration page.<br/>Default: We were unable to complete your registration. Please verify the information provided and try again.                                                                                                                                          |
