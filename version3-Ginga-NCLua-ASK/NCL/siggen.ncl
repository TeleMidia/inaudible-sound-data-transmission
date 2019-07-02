<ncl>
  <head>
    <regionBase>
      <region id="screenReg" width="100%" height="100%" zIndex="2"/>
    </regionBase>
    <descriptorBase>
      <descriptor id="screenDesc" region="screenReg"/>
    </descriptorBase>
    <connectorBase>
      <importBase documentURI="causalConnBase.ncl" alias="conEx"/>
    </connectorBase>
  </head>
  <body>
    <port id="start" component="m"/>
    <media id="m" src="media/Audio-Track_-13.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m2" src="media/Audio-Track_-01.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m3" src="media/Audio-Track_-02.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m4" src="media/Audio-Track_-03.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m5" src="media/Audio-Track_-04.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m6" src="media/Audio-Track_-05.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m7" src="media/Audio-Track_-06.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m8" src="media/Audio-Track_-07.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m9" src="media/Audio-Track_-08.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m10" src="media/Audio-Track_-09.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m11" src="media/Audio-Track_-10.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m12" src="media/Audio-Track_-11.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="m13" src="media/Audio-Track_-12.mp3">
      <property name="volume" value="1"/>
    </media>
    <media id="image" src="app_img.png" descriptor="screenDesc"/>

    <link  id="Play" xconnector="conEx#onBeginStart">
      <bind role="onBegin" component="m"/>
      <bind role="start" component="image"/>
      <bind role="start" component="m2"/>
      <bind role="start" component="m3"/>
      <bind role="start" component="m4"/>
      <bind role="start" component="m5"/>
      <bind role="start" component="m6"/>
      <bind role="start" component="m7"/>
      <bind role="start" component="m8"/>
    </link>

    <link id="End1_Start2" xconnector="conEx#onEndStart">
      <bind role="onEnd" component="m"/>
      <bind role="start" component="m2"/>
    </link>

    <link id="End2_Start3" xconnector="conEx#onEndStart">
      <bind role="onEnd" component="m2"/>
      <bind role="start" component="m3"/>
    </link>

    <link id="End3_Start4" xconnector="conEx#onEndStart">
      <bind role="onEnd" component="m3"/>
      <bind role="start" component="m4"/>
    </link>

    <link id="End4_Start5" xconnector="conEx#onEndStart">
      <bind role="onEnd" component="m4"/>
      <bind role="start" component="m5"/>
    </link>

    <link id="End5_Start6" xconnector="conEx#onEndStart">
      <bind role="onEnd" component="m5"/>
      <bind role="start" component="m6"/>
    </link>
    <link id="End6_Start7" xconnector="conEx#onEndStart">
      <bind role="onEnd" component="m6"/>
      <bind role="start" component="m7"/>
    </link>
    <link id="End7_Start8" xconnector="conEx#onEndStart">
      <bind role="onEnd" component="m7"/>
      <bind role="start" component="m8"/>
    </link>
  </body>
</ncl>