# Developers guide

To start developing for esthesis EDGE, first you need to set up a [development environment](Dev-environment-setup.md).

## Creating new modules

esthesis EDGE is designed to be modular, so you can create new modules to extend its functionality. Modules allow
you to add new features, fetching data from any third-party system you can communicate with. However, before creating
a new module, you should take into account whether the specific third-party system you want to integrate with is
of a generic-enough nature, to be part of esthesis EDGE. For private or very specific third-party systems, it is
recommended to integrate them with an esthesis CORE agent running alongside them. At any rate, before you decide to
create a new module, you can open a new issue on our [GitHub](https://github.com/esthesis-iot/esthesis-edge/issues)
to discuss it first.

## Module structure

Regarding the functionality of a new module, you are pretty much free to implement it as you see fit. However, there
are some guidelines you should follow to make sure your module is compatible with esthesis EDGE.

New modules should be placed into their own package under `esthesis.edge.modules.modulename`, for example
`esthesis.edge.modules.weather`. Create a main module entry-class, using a method such as
`public void onStart(@Observes StartupEvent ev)`, in order to provide some startup information to the users of
esthesis EDGE on your module. A message like "XYZ module is enabled." is recommended, however you can provide any
information you see fit.

### Configuration

Depending on the nature of your module, make sure you provide ample configuration options for the users; you should
think how other users might use your module and what kind of configuration options they might need. For example, if
you are creating a module that fetches weather data, you should provide configuration options for the user to set
the location, the units, the API key, etc. You can add your module's configuration options in esthesis EDGE
main configuration file, `application.yaml`, under a new section named `esthesis.edge.modules.modulename`.

### Endpoints

If your module needs to expose REST endpoints, you can create them by annotating them as JAX-RS resources. The
names of your endpoints should be prefixed with the name of your module, for example `weather/forecast`.

You can create public and admin endpoints. Public endpoints can be accessed by any user, while admin endpoints
require the user to be authenticated using the esthesis EDGE secret token.

esthesis EDGE provides two annotations that can be useful when creating your module's endpoints:

- `@ModuleEndpoint(enabledProperty = "esthesis.edge.modules.mymodule.enabled")`
  The `ModuleEndpoint` annotation, allows you to enable or disable your module's endpoints by setting the value of
  the `enabledProperty` to `true` or `false` in the `application.yaml` configuration file. This annotation is mandatory,
  so that users can selectively enable or disable your module.
- `@AdminEndpoint`
  The `AdminEndpoint` annotation, allows you to create an endpoint that requires the user to be authenticated using the
  esthesis EDGE secret token. This annotation is optional, and you can use it only if you need to create an endpoint
  that requires authentication.

<tip>
Please do not forget to provide complete OpenAPI documentation for your module's endpoints.
</tip>

## Creating devices

esthesis EDGE allows you to fetch data from third-party systems and store in a local InfluxDB. However, one of esthesis
EDGE's main advantages is that it integrates flawlessly with esthesis CORE. That effectively means that you can set up
esthesis EDGE once, "forget" about it, and then manage your data directly in esthesis CORE - where you have many
additional options on how to manage your data, where to store it, how to visualise it, etc.

For such an integration to take place, esthesis EDGE follows the same "device" concept as esthesis CORE. For every
third-party system that is connected to esthesis EDGE, a virtual device is created in esthesis CORE. This device
represents the third-party system and all data fetched from it is stored in the device's data points. In case you create
a module that may represent multiple users, you should create a device for each user. It is up to you to ensure that
device names (i.e. hardware IDs) are unique for your own module but also for esthesis CORE and EDGE as a whole. A good
naming strategy is to prefix hardware IDs with the name of your module, followed by a unique identifier for the user.

#### Devices configuration

You can maintain device-specific configuration in esthesis EDGE. This configuration is stored in the MariaDB of esthesis
and allows you to store any configuration options you need for your devices. You can maintain any number of
configuration
options for each device, and you can update them at any time.

<tip>
esthesis EDGE provides a DeviceService class that can help you create devices and maintain their configuration.
</tip>

## Queuing data

esthesis EDGE works in [two phases](How-it-works.md): The first phase is the data collection phase, where data is
collected from the third-party systems, and the second phase is the data synchronisation phase, where the data is
synchronised with a local InfluxDB and/or esthesis CORE. Those two phases are separated by a queue held in the MariaDB
of esthesis EDGE, where the data is stored until it is synchronised.

The way in which data is fetched as well as all necessary data structures to support this are totally up to you.
To be able to queue your data for esthesis EDGE to synchronise you should use the esthesis Line Protocol 
(eLP). The specification of eLP can be found in esthesis CORE and esthesis EDGE provide helper classes to make it easier 
for you to create eLP representations our of your own data structures via tha `QueueService` class.

You can prepare a new item to be queued using the `QueueItem` class. `QueueService` class provides a 
`queue(QueueItemDTO queueItemDTO)` method allowing you to persist your data in the queue. eLP comes into play in the
`dataObject` property of the `QueueItemDTO` class. You can create eLP-compatible `dataObject` properties using the 
provided `ELPEntry` class and its builder, for example:
```
ELPEntry.builder()
  .category("mycategory")
  .date(Instant.now())
  .measurement("12i")
  .build()
  .toString()
```

Each `dataObject` might contain multiple eLP entries separated by a new line character, provided all such entries
belong to the same device.