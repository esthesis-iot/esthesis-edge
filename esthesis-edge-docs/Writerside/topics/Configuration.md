# Configuration

esthesis EDGE configuration comes in two parts: The configuration of esthesis EDGE itself, and the configuration of the
supported modules. 

According to the initial installation method (standalone container, compose, or Kubernetes), 
configuration parameters should be specified appropriately.
- Parameter names in capital, indicate environment variables that can be set when setting up esthesis EDGE in a
  container engine or via Compose.
- Parameter names in lowercase, indicate configuration options that can be set when setting up esthesis EDGE in Kubernetes.


You can find the configuration options in the following sections of the documentation:
- [](Main.md)
- [Enedis module configuration](enedis.md)