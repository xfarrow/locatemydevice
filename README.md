# Locate My Device
This app helps to locate a lost smartphone through the use of conventional SMS. **It is in beta and under active development**

## Screenshots
| Homescreen | SMS Chat
|:-:|:-:|
| <img src="/images/home.png" alt="Homescreen" width="190" height="390.641"> | <img src="/images/sms.png" width="190" height="390.641"> |

## Usage
Send an SMS to the device you want to locate to retrieve information about it.

**Synopsis**
<br/>
```activation_command password option```

**Default values**
<br/>
* The default value for ```activation_command``` is ```LMD```
* The default value for ```password``` is ```0000```. It's highly encouraged to change it

**Options**
Option | Explaination 
-------|:------------|
locate | Will return the most accurate set of coordinates possible <br/> and a link to them pinpointed to OpenStreetMap |
cellinfo | Will return a set of uniquely identifiable information about cell towers near the phone. <br/> You can then put this information on [OpenCellId][1] to individuate the smartphone's <br/> approximate location. Requires enabled location |
battery | Will return battery infos |

[1]: https://opencellid.org/
