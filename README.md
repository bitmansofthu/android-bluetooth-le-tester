# Bluetooth Low Energy Tester for Android

## Introduction
This Android application tests the access to your LE-device. So it scans devices, and connects to the selected LE-device. If connection is established successfully, tester lists BT GATT-services in expandable list, and expands for showing GATT-characteristics of the selected service. Application has been written in Java in AndroidStudio environment. Application is designed for Android OS versions >=8. Naturally supported Java API-functions (non-deprecated ones) are used for each action (read, write etc), taken in account Android OS version, where this application is executed.

In OS versions >=12, runtime permissions for scanning and connecting are needed. Application will inform the user (in two first starts of the program) to enable those permissions. Application cannot be used without accepting those permissions.

Here is screenshot of Scannning view:

<img width="150" alt="" src="https://github.com/tikanpet/android-bluetooth-le-tester/assets/128066969/5445fdca-f14c-456f-ab91-f79e71d0f0eb">


## Showing GATT-attributes
Each characteristics value (GATT-attribute) is shown in Edit-text box. So each value can be read or written via this box. Also unsolicited notifications from your remote device are supported. Every attribute doesn't naturally has all accesses (read, write, notification), so when services and characteristics are discoved after connection-establishment, supported accesses will be gathered from this data. Corresponding radio-buttons will then show up in UI for each attribute. Desired access can then be selected using corresponding radio-button. In addition, confirmation button is available for each attribute for triggering the read-request or write-send. When enabling the notification, confirmation button will be hidden, and corresponding GATT-value will update automatically in Text-box. There is also always available OFF-button for disabling all accesses.

## Formatting values of GATT-attributes

There is drop down-menu (spinner) for each attribute for changing the format, how the value will be presented in Text-box. Formats are:

**'HEX', '+-INT', '+INT', 'ASCII', 'FLOAT'**.

Regardless of the selected format, value to be written is edited as hex decimal.

## Refreshing local cache
Android stores in system level the services and characteristics of each peripheral to peripheral specific cache-file, where from the data can be collected to prevent consecutive discovering procedure (quite slow) from the remote device. But some times this cache file might be out of synch with corresponding data in remote device (e.g. because of some changes to attributes in remote device). Application has selection for refreshing the cache if needed. Checkbox need to be enabled before connecting to your peripheral.

## Solving logical names for corresponding UUID
It might sometimes be hard for the user to figure out using UUIDs, in what purpose each attribute is needed for. So, application also tries to find out the logical name of each service/characteristic. I added two YAML-files in assets-folder (services_uuids.yaml, characteristic_uuids.yaml), originated by BT SIG (add www-link...). I implemented parser for gathering logical name for each UUID, which is shown in UI. Notice that these logical names/UUID are only available for **reserved UUIDs by BT SIG**, having base part as bolded:

long 48 bit **base** address = xxxxyyy-**0000-1000-8000-00805f9b34fb**

Short 16-bit address is written from YAML-file to 'yyyy' field (xxxx=0000).
If this filled 48-bit MAC-address is same as some discovered service or characteristic, logical name is found. Otherwise UUID will be shown with 'UNKNOWN' name.

Notice, that any customer specific addresses has different address in base part as reserved base address above, so customer addresses have been excluded from YAML-files.
