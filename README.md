# Locate My Device
This app helps to locate a lost smartphone through the use of conventional SMS. 

<a href="https://github.com/xfarrow/locatemydevice/releases/latest"><img src="http://yt3dl.net/images/apk-download-badge.png" alt="Get it on Github" height="100"></a>
<a href="https://apt.izzysoft.de/fdroid/index/apk/com.xfarrow.locatemydevice"><img src="images/IzzyOnDroid_badge.png" alt="Get it on IzzyOnDroid" height="100"></a>
<!--a href="https://f-droid.org/packages/com.xfarrow.locatemydevice/"><img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="100"></a-->

## Screenshots
| Homescreen | SMS Chat
|:-:|:-:|
| <img src="/images/home.png" alt="Homescreen" width="190" height="390.641"> | <img src="/images/sms.png" width="190" height="390.641"> |

## Usage
Send an SMS to the device you want to locate in order to retrieve information about it.

**Synopsis**
<br/>
```activation_command password option```

**Default values**
<br/>
* The default value for ```activation_command``` is ```LMD```
* The default value for ```password``` is ```0000```. It's highly encouraged to change it

**Options**
Option | Explaination | Required permission
-------|:------------|--------------------|
locate | Will return the most accurate set of coordinates possible and a link to them pinpointed to OpenStreetMap | Location
cellinfo | Will return a set of uniquely identifiable information about cell towers near the phone. You can then put this information on [OpenCellId][1] to individuate the smartphone's approximate location | Location
battery | Will return battery infos | None |
lock | Will  lock down the smartphone | Device Administrator |
show "message" | Will show a message on the screen, even when it's locked. | Overlay |
callme | You will receive a call from the lost smartphone | Calls, Overlay |
wifi | Will return Wi-Fi infos | Location |
wifi-on | Will enable Wi-Fi (Only API < 29) | None |
wifi-off | Will disable Wi-Fi (Only API < 29) | None |
gps-on | Will enable GPS.  | WRITE_SECURE_SETTINGS
gps-off | Will disable GPS. | WRITE_SECURE_SETTINGS
ring | Will make the smartphone ring | Overlay |

### Auto enabling location (only 1.1-beta)

The app is able to automatically enabe location if it is off, but you need to grant a specific permission through adb.

To grant the permission you need to do the following:
1. Install ADB (https://developer.android.com/studio/releases/platform-tools.html)
2. Activate Developer options on your phone (https://developer.android.com/studio/debug/dev-options#enable)
3. In the Developer options enable USB debugging (https://developer.android.com/studio/debug/dev-options#debugging)
4. Connect your phone with your computer via USB
5. On your computer open a terminal, change to the directory where you extracted the platform tools and run the following command

```
adb shell pm grant com.xfarrow.locatemydevice android.permission.WRITE_SECURE_SETTINGS
```


## Security
### Security measures in place

This application manages sensitive data. As such, it strives to be as secure as possible. There are two walls to resist a potential malicious individual:
* Password (mandatory): The default password is ```0000```. Change it to something more secure to enhance your protection. Furthermore, you probably know that SMS are not encrypted. This means that it is advised to change your password when you send to your smartphone messages containing it in order to deny access to attackers as soon as possible (unlikely and costly attack, yet let's try to prevent any displeasing scenario).
* Whitelist (optional): Whitelist numbers are the only numbers that the app will accept communications from.

### Legit apks
GitHub's releases section is the only place where I am uploading apks. Packages on F-Droid are compiled by the repo's owner.

### Antivirus' false positives
Some antivirus falsely report it's a malware due to the app's extensive demanding permissions.

## More info
There is already Google Find My Device, but I wanted to develop a free and open source alternative (even tho it is suggested to keep Google's, unless you do not have Play Services and/or wanting to go full-in about privacy). There is already a good FOSS alternative, [Find My Device](https://gitlab.com/Nulide/findmydevice) (from which I took inspiration from), but wanted to change it a little. I did [fork](https://www.github.com/xfarrow/FindMyDevice) it, but eventually decided to create my own project.

**Other:**
* This software is licensed under the GNU General Public License v3.0. Click [here](https://github.com/xfarrow/locatemydevice/blob/main/LICENSE) for more information;
* As specified in the license, the software is provided "as is" with no warranty;
* This software is not meant to be the only installed device locator. It is strongly advised to use it in conjunction with Google Find My Device; in fact it might suffer from a bug or might be unresponsive when you need it the most;
* The logo has been provided to  me for free by [Iconfinder](https://www.iconfinder.com/);

## Support
If you enjoy this software, a little star is highly appreciated: it shows me people are interested in it.

You can also donate if you want to, using the button below

<noscript><a href="https://liberapay.com/xfarrow/donate"><img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg"></a></noscript>

[1]: https://opencellid.org/
