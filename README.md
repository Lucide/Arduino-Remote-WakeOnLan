# Remote Wake On Lan, through a **HTTP** Captive Portal protected network, with Arduino

This was an experiment to remotely turn on my PC while it was at the dormitory, under the local protected network. Router and firewall were obviously out of reach, and everything was closed to external access.

## The Arduino

To achieve a working remote WOL, the communication had to be reversed, a device from inside the network had to initiate the communication to the outside.\
This role is played by the Arduino, which must also keep its internet access active by interacting with the local captive portal.

Great care was put in the Arduino routine to be as resilient as possible. I got a chance to take a closer look to the HTTP protocol and write headers by hand.

## The Java Trigger Server

The Arduino attempts to connect to a remote server every 60 seconds. If it succeeds, a WOL magic packet is sent through UDP to the configured subnet broadcast address.

Why the broadcast address?\
Ip addresses were dhcp assigned.

The trigger server is launched on the controlling pc when required, and must be exposed to the internet (perhaps temporarily). It's only job is to accept connections, so it can be closed as soon as the light turns green.
The light turns green only if a client sends a "lalilulelo" message, to filter out eventual random connections from the internet.

## Why this was never actually used

Despite my numerous (naive) tests before starting any work, it turned out that the local captive portal didn't (rightfully) support plain HTTP after all.\
I had underestimated the infrastructure, which some time later I saw was actually quite advanced. And the more time passes, the less I expect to encounter http forms.\
The Arduino I was using (Uno/Leonardo) did **not** have the power to handle HTTPS.

I ended up buying a new laptop some time later anyway.
