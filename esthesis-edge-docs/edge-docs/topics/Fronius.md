# Fronius module

The esthesis EDGE Enedis module allows you to fetch data from the [Fronius Solar API](https://www.fronius.com/~/downloads/Solar%20Energy/Operating%20Instructions/42,0410,2012.pdf).

The data the module supports includes:
- Power flow Daily Production

## Initial registration and data fetch
New devices should be declared using the proper parameters in the esthesis Fronius module configuration. For each declared device, 
the Fronius module will register the device, if it is new and then try to fetch data from the Solar API. Previous registered devices
which are not declared in the configuration will be disabled and will not be able to fetch data anymore.

## Configuration
For a full list of configuration options for the Fronius module, please refer to the [Fronius module configuration](FroniusConfiguration.md).