<h1> android network location :iphone:	:world_map:	 </h1>
<p>
<b>It is a littel part of my final project. <br>
This android activity simply displays user location (according to NETWORK_PROVIDER) on the screen <br>
and sends it to the connected server by WebSocket protocol (socket.io library) </b>  <br>
  
<b> Pay attention:</b>
 <li> It is the java source code only (without the gradle, manifest and the rest of the files</li>
 <li> For adding a srver - Change the address in the configuration file</li>
 <li> You need to open the necessary access in androidmanifest.xml file</li>
</p>
<hr>
<p>
 <b> If the network is not available you can uncomment the following line and us the GPS_PROVIDER:</b>
<img src='https://github.com/Michal961/android-network-location/blob/main/img/netOrGps.png' width='600'>
</p>
<hr>
<p>
<b> Some screenshots:</b><br><br>
<img src='https://github.com/Michal961/android-network-location/blob/main/img/not_connected.PNG' width='300'>
<img src='https://github.com/Michal961/android-network-location/blob/main/img/enter.png' width='305'>
<img src='https://github.com/Michal961/android-network-location/blob/main/img/location.png' width='304'>
</p>
<hr>
<p>
<b>Optional - Connect to some server and send your locatin:</b><br><br>
<img src='https://github.com/Michal961/android-network-location/blob/main/img/connaction.png' width='450'>
<img src='https://github.com/Michal961/android-network-location/blob/main/img/sendLocatin.png' width='450'>
</p>

<p>
  <b>In the server: (Written in Node.js) </b><br><br>
<img src='https://github.com/Michal961/android-network-location/blob/main/img/serverConnaction.png' width='450'>
<img src='https://github.com/Michal961/android-network-location/blob/main/img/serverLocation.png' width='450'>
</p>


