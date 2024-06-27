This is an example of how permissions can be handled when they are requested via buttons on the screen. I included 2 runtime permissions and 1 appop (system) permission.<br>
How the app looks like:<br>
![Screenshot](https://github.com/DarkZodiAk/PermissionHandlingExample2/assets/112490256/f3f59b87-27e1-41e5-8689-5c99cfc80fc2)
<br><br>
To do this experiment I used theory from my previous [experiment](https://github.com/DarkZodiAk/PermissionHandlingExample). Now to define, whether permission has state NOT_REQUESTED or PERMANENTLY_DECLINED, we need to remember that. 
I used DataStore for this goal. NOT_REQUESTED state is a default state for all permissions at first app startup.<br><br>

What have I learnt while doing this experiment:
1. System permissions only have 2 states: granted and not granted. To determine state I used AppOpsManager;<br>
2. To grant system permission user should go to settings. I used Activity Result API and sent user to settings whenever he wants to grant PACKAGE_USAGE_STATS permission;<br>
3. The OS remembers if runtime permission was declined at once (or at least once in Android version < 11) even when the app was terminated.
