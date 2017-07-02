# TimerMqWay
[Data-driven] [micro-service] for Time based activities, based on ch.quantasy.mqtt.gateway.

ch.quantasy.timer.mqtt.gateway

The underlying idea of TimerMqWay is a self-explaining micro-service, providing a data-driven interface
 to timer based activities. This way, the implementation is agnostic to the programming-language and paradigm used for orchestration. Any of which fits, as long as you master it.

Please note, that this project depends on [https://github.com/knr1/ch.quantasy.mqtt.gateway]



## Installation
In order to install TimerMqWay 
* **Developers way** clone and build the project. Please note that it depends on [https://github.com/knr1/ch.quantasy.mqtt.gateway]
* **Users way** download the latest [TimerMqWay.jar]
 
## Usage
To run TimerMqWay,you need Java (7 or higher) and a running MQTT-Server. You can start TimerMqWay with the MQTT-Server-Parameter.
However, you can program your business logic in any programming language that can communicate over MQTT.


INTENT_CONFIGURATION, "id: <String>\n first: [null|0.." + Long.MAX_VALUE + "]\n repeat: [null|1.." + Long.MAX_VALUE + "]\n last: [null|0.." + Long.MAX_VALUE + "]\n");

Then run the following command in order to use an MQTT-Server at localhost
```sh
$ java -jar TimerMqWay.jar tcp://127.0.0.1:1883
```
Or, if you do not have any MQTT-Broker ready, use an existing one at iot.eclipse.org:1883 (Not recommended as it is an open server everyone can read and write into)
```sh
$ java -jar TimerMqWay.jar tcp://iot.eclipse.org:1883
```

Then, all timer related things run under the topic: Timer/Tick/[instance]

The status immediately shows the current UNIX epoch time, which is updated once per second.
```sh
Topic: Time/Tick/U/[instance]/S/unixEpoch
Message: ---
         milliseconds: [0..9223372036854775807]
```

There is only one intent in order to setup or alter a timer: 
```sh
Topic: Timer/Tick/U/[instance]/I/configuration
Message: --- 
         id: <String>
         epoch: [null|0..9223372036854775807]
         first: [null|0..9223372036854775807]
         interval: [null|1..9223372036854775807]
         last: [null|0..9223372036854775807]
```

 * id: discriminator which identifies a specific timer configuration. 
 * epoch: The absolute time in UNIX epoch, which will be used as reference. If no epoch is given, everything is meant for 'now'.
 * first: first tick in milliseconds since the set epoch. Can be avoided in order to start at epoch.
 * interval: interval between two 'ticks' in milliseconds. If interval is not given, no intermediate ticks will be given
 * last: last tick in milliseconds since the set epoch. Can be avoided in order to create an everlasting ticking.


### Quick Shots
In the following, some one-liners are shown in order to demonstrate how easy it is to set a timer.
In order to see who is calling, the intent will always end with '/quickshot' as the intending party (you can put anything (or nothing) there).
We assume that the Timer-Service runs on a server called matrix. 
 
**Setting a timer that starts immediately for a one shot.**
```
Topic: Timer/Tick/U/matrix/I/configuration/quickshot
Message: id: thisIsAOneTimer
```

The answer will be the following event:
```
Topic: Timer/Tick/U/matrix/E/thisIsAOneTimer
Message: --- - timestamp: 1490217018902 value: 0 
```

**Setting a timer that repeats every 500ms ASAP.**
```
Topic: Timer/Tick/U/matrix/I/configuration/quickshot
Message: id: fiveHundertMillis
         interval: 500
```


The answer will be the following event:
```
Topic: Timer/Tick/U/matrix/E/fiveHundertMillis
Message: --- - timestamp: 1490217018902 value: 501 
```

whereas the value represents the time in ms passed, since the set epoch.

**Setting a timer that repeats once a second, starting 10 seconds from ASAP.**
```
Topic: Timer/Tick/U/matrix/I/configuration/quickshot
Message: id: onceASecond
         first: 10000
         interval: 1000
```


The answer will be the following event:
```
Topic: Timer/Tick/U/matrix/E/onceASecond
Message: --- - timestamp: 1490217018902 value: 10001 
```

whereas the value represents the time in ms passed, since the set epoch.


**Setting a timer that repeats once a second, starting 10 seconds from an exact time (in the future).**
Say the actual time would be: 1490217018902
And we want to set the epoch of the timer 20 seconds later: 1490217038902
```
Topic: Timer/Tick/U/matrix/I/configuration/quickshot
Message: id: onceASecond
         epoch: 1490217038902
         first: 10000
         interval: 1000
```


The answer will be the following event:
```
Topic: Timer/Tick/U/matrix/E/onceASecond
Message: --- - timestamp: 1490217048902 value: 10000 
```

whereas the value represents the time in ms passed, since the set epoch.


**Setting a timer that repeats once a second, starting 10 seconds from an exact time (in the future) finishing 20 seconds from the exact time (in the future).**
Say the actual time would be: 1490217018902
And we want to set the epoch of the timer 20 seconds later: 1490217038902

```
Topic: Timer/Tick/U/matrix/I/configuration/quickshot
Message: id: onceASecond
         epoch: 1490217038902
         first: 10000
         interval: 1000
         last: 20000
```

The answer will be the following event:
```
Topic: Timer/Tick/matrix/E/onceASecond
Message: --- - timestamp: 1490217048902 value: 10000 
```

whereas the value represents the time in milliseconds passed, since the set epoch.
Caution: This means, the timer is 'only' running for 10 seconds.
Caution: First and Last is meant to be the amount of milliseconds since the set epoch!

## API

### Tick
```
Timer/Tick/U/<id>/E/tick/<id>
   timestamp: [0..9223372036854775807]
    value: [0..9223372036854775807]
   
```
```
Timer/Tick/U/<id>/I/configuration
   id: <String>
    first: [null|0..9223372036854775807]
    interval: [null|1..9223372036854775807]
    last: [null|0..9223372036854775807]
   
```
```
Timer/Tick/U/<id>/S/configuration/<id>
   id: <String>
    first: [null|0..9223372036854775807]
    interval: [null|1..9223372036854775807]
    last: [null|0..9223372036854775807]
   
```
```
Timer/Tick/U/<id>/S/unixEpoch
   milliseconds: [0..9223372036854775807]
   
```



[https://github.com/knr1/ch.quantasy.mqtt.gateway]:<https://github.com/knr1/ch.quantasy.mqtt.gateway>
[TimerMqWay.jar]: <https://github.com/knr1/ch.quantasy.tinkerforge.mqtt.gateway/blob/master/dist/TimerMqWay.jar>

