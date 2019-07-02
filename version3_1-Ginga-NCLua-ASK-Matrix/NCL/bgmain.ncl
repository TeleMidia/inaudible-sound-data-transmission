<?xml version="1.0" encoding="UTF-8"?>
<ncl >
<head>
	<regionBase>
      	<region id="screenReg" width="100%" height="100%" zIndex="2"/>		
		<region id="rgLua"   width="0%" height="0%" zIndex="1"/>		  
	</regionBase>

	<descriptorBase>
      	<descriptor id="screenDesc" region="screenReg"/>
		<descriptor id="dsLua"   region="rgLua" focusIndex="ixLua"/>
	</descriptorBase>

	<connectorBase>
		<causalConnector id="onBeginSet">
			<connectorParam name="var"/>
			<simpleCondition role="onBegin"/>
			<simpleAction role="set" value="$var"/>
		</causalConnector>
		<causalConnector id="onKeySelectionStartNStop">
			<connectorParam name="keyCode"/>
			<simpleCondition role="onSelection" key="$keyCode"/>
			<compoundAction operator="seq">
				<simpleAction role="start" max="unbounded"/>
				<simpleAction role="stop"/>
			</compoundAction>
		</causalConnector>
        <causalConnector id="onKeySelectionStart">
			<connectorParam name="keyCode"/>
			<simpleCondition role="onSelection" key="$keyCode"/>
			<simpleAction role="start" max="unbounded"/>
		</causalConnector>
		<causalConnector id="onEndStopN">
			<simpleCondition role="onEnd"/>
			<simpleAction role="stop" max="unbounded"/>
		</causalConnector>
		<causalConnector id="onEndStart">
			<simpleCondition role="onEnd"/>
			<simpleAction role="start"/>
		</causalConnector>
        <causalConnector id="onCondEqBeginTestSet">
          <connectorParam name="var"/>
          <compoundCondition operator="and">
            <simpleCondition role="onEndAttribution"/>
            <assessmentStatement comparator="eq">
              <attributeAssessment role="test"/>
              <valueAssessment value="$varValue"/>
            </assessmentStatement>
          </compoundCondition>
          <simpleAction role="set" value="$var"/>
        </causalConnector>
        <causalConnector id="onCondEqBeginTestStart">
          <connectorParam name="var"/>
          <compoundCondition operator="and">
            <simpleCondition role="onEndAttribution"/>
            <assessmentStatement comparator="eq">
              <attributeAssessment role="test"/>
              <valueAssessment value="$varValue"/>
            </assessmentStatement>
          </compoundCondition>
          <simpleAction role="start"/>
        </causalConnector>
    </connectorBase>
</head>

<body>
	<port id="entryPort" component="image"/>

	<!-- MEDIAS -->

	<media type="application/x-ginga-settings" id="programSettings">
		<property name="service.currentKeyMaster"/>
	</media>

	<media id="audio13" src="media/Audio-Track_-13.wav">
		<property name="volume" value="0"/>
	</media>
	<media id="audio1" src="media/Audio-Track_-01.wav">
		<property name="volume" value="0"/>
	</media>
	<media id="audio2" src="media/Audio-Track_-02.wav">
		<property name="volume" value="0"/>
	</media>
	<media id="audio3" src="media/Audio-Track_-03.wav">
		<property name="volume" value="0"/>
	</media>
	<media id="audio4" src="media/Audio-Track_-04.wav">
		<property name="volume" value="0"/>
	</media>
	<media id="audio5" src="media/Audio-Track_-05.wav">
		<property name="volume" value="0"/>
	</media>
	<media id="audio6" src="media/Audio-Track_-06.wav">
		<property name="volume" value="0"/>
	</media>
	<media id="audio7" src="media/Audio-Track_-07.wav">
		<property name="volume" value="0"/>
	</media>
	<media id="audio8" src="media/Audio-Track_-08.wav">
		<property name="volume" value="0"/>
	</media>

	<media id="lua"  src="freqs_bg.lua" descriptor="dsLua">
		<property name="f1" value="0"/>
		<property name="f2" value="0"/>
		<property name="f3" value="0"/>
		<property name="f4" value="0"/>
		<property name="f5" value="0"/>
		<property name="f6" value="0"/>
		<property name="f7" value="0"/>
		<property name="f8" value="0"/>
        <property name="f13" value="0"/>
        <property name="matrix" value="0"/>
	</media>

    <media id="image" src="app_img.png" descriptor="screenDesc"/>	
    <media id="video_matrix" src="matrix.mp4" descriptor="screenDesc"/>

	<!-- LINKS -->

	<link xconnector="onKeySelectionStartNStop">
		<bind role="onSelection" component="image">
			<bindParam name="keyCode" value="RED"/>
		</bind>
		<bind role="start" component="lua"/>
		<bind role="start"   component="audio1"/>
		<bind role="start"   component="audio2"/>		
		<bind role="start"   component="audio3"/>		
		<bind role="start"   component="audio4"/>		
		<bind role="start"   component="audio5"/>		
		<bind role="start"   component="audio6"/>		
		<bind role="start"   component="audio7"/>		
		<bind role="start"   component="audio8"/>		
		<bind role="start"   component="audio13"/>		
		<bind role="stop"  component="image"/>
	</link>

    <link xconnector="onCondEqBeginTestStart">
		<bind role="onEndAttribution" component="lua" interface="matrix"/>
		<bind role="test" component="lua" interface="matrix">
			<bindParam name="varValue" value="1"/>
      	</bind>  
		<bind role="start" component="video_matrix"/>
	</link>

	<link xconnector="onBeginSet">
		<bind role="onBegin" component="lua"/>
		<bind component="programSettings" interface="service.currentKeyMaster" role="set">
			<bindParam name="var" value="ixLua"/>
		</bind>
    </link>

    <link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f13"/>
		<bind role="test" component="lua" interface="f13">
			<bindParam name="varValue" value="1"/>
      	</bind>  
		<bind role="set" component="audio13" interface="volume">
       		<bindParam name="var" value="1"/>
      	</bind>
	</link>
	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f13"/>
		<bind role="test" component="lua" interface="f13">
			<bindParam name="varValue" value="0"/>
      	</bind>  
		<bind role="set" component="audio13" interface="volume">
       		<bindParam name="var" value="0"/>
      	</bind>
	</link>

	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f1"/>
		<bind role="test" component="lua" interface="f1">
			<bindParam name="varValue" value="1"/>
      	</bind>  
		<bind role="set" component="audio1" interface="volume">
       		<bindParam name="var" value="1"/>
      	</bind>
	</link>
	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f1"/>
		<bind role="test" component="lua" interface="f1">
			<bindParam name="varValue" value="0"/>
      	</bind>  
		<bind role="set" component="audio1" interface="volume">
       		<bindParam name="var" value="0.3"/>
      	</bind>
	</link>

	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f2"/>
		<bind role="test" component="lua" interface="f2">
			<bindParam name="varValue" value="1"/>
      	</bind>  
		<bind role="set" component="audio2" interface="volume">
       		<bindParam name="var" value="1"/>
      	</bind>
	</link>
	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f2"/>
		<bind role="test" component="lua" interface="f2">
			<bindParam name="varValue" value="0"/>
      	</bind>  
		<bind role="set" component="audio2" interface="volume">
       		<bindParam name="var" value="0.3"/>
      	</bind>
	</link>

	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f3"/>
		<bind role="test" component="lua" interface="f3">
			<bindParam name="varValue" value="1"/>
      	</bind>  
		<bind role="set" component="audio3" interface="volume">
       		<bindParam name="var" value="1"/>
      	</bind>
	</link>
	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f3"/>
		<bind role="test" component="lua" interface="f3">
			<bindParam name="varValue" value="0"/>
      	</bind>  
		<bind role="set" component="audio3" interface="volume">
       		<bindParam name="var" value="0.3"/>
      	</bind>
	</link>

	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f4"/>
		<bind role="test" component="lua" interface="f4">
			<bindParam name="varValue" value="1"/>
      	</bind>  
		<bind role="set" component="audio4" interface="volume">
       		<bindParam name="var" value="1"/>
      	</bind>
	</link>
	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f4"/>
		<bind role="test" component="lua" interface="f4">
			<bindParam name="varValue" value="0"/>
      	</bind>  
		<bind role="set" component="audio4" interface="volume">
       		<bindParam name="var" value="0.3"/>
      	</bind>
	</link>

	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f5"/>
		<bind role="test" component="lua" interface="f5">
			<bindParam name="varValue" value="1"/>
      	</bind>  
		<bind role="set" component="audio5" interface="volume">
       		<bindParam name="var" value="1"/>
      	</bind>
	</link>
	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f5"/>
		<bind role="test" component="lua" interface="f5">
			<bindParam name="varValue" value="0"/>
      	</bind>  
		<bind role="set" component="audio5" interface="volume">
       		<bindParam name="var" value="0.3"/>
      	</bind>
	</link>

	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f6"/>
		<bind role="test" component="lua" interface="f6">
			<bindParam name="varValue" value="1"/>
      	</bind>  
		<bind role="set" component="audio6" interface="volume">
       		<bindParam name="var" value="1"/>
      	</bind>
	</link>
	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f6"/>
		<bind role="test" component="lua" interface="f6">
			<bindParam name="varValue" value="0"/>
      	</bind>  
		<bind role="set" component="audio6" interface="volume">
       		<bindParam name="var" value="0.3"/>
      	</bind>
	</link>

	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f7"/>
		<bind role="test" component="lua" interface="f7">
			<bindParam name="varValue" value="1"/>
      	</bind>  
		<bind role="set" component="audio7" interface="volume">
       		<bindParam name="var" value="1"/>
      	</bind>
	</link>
	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f7"/>
		<bind role="test" component="lua" interface="f7">
			<bindParam name="varValue" value="0"/>
      	</bind>  
		<bind role="set" component="audio7" interface="volume">
       		<bindParam name="var" value="0.3"/>
      	</bind>
	</link>

	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f8"/>
		<bind role="test" component="lua" interface="f8">
			<bindParam name="varValue" value="1"/>
      	</bind>  
		<bind role="set" component="audio8" interface="volume">
       		<bindParam name="var" value="1"/>
      	</bind>
	</link>
	<link xconnector="onCondEqBeginTestSet">
		<bind role="onEndAttribution" component="lua" interface="f8"/>
		<bind role="test" component="lua" interface="f8">
			<bindParam name="varValue" value="0"/>
      	</bind>  
		<bind role="set" component="audio8" interface="volume">
       		<bindParam name="var" value="0.3"/>
      	</bind>
	</link>

	<link xconnector="onEndStart">
		<bind role="onEnd" 	 component="audio1"/>
		<bind role="start"   component="audio1"/>		
		<bind role="start"   component="audio2"/>		
		<bind role="start"   component="audio3"/>		
		<bind role="start"   component="audio4"/>		
		<bind role="start"   component="audio5"/>		
		<bind role="start"   component="audio6"/>		
		<bind role="start"   component="audio7"/>		
		<bind role="start"   component="audio8"/>		
		<bind role="start"   component="audio13"/>	
	</link>

	<link xconnector="onEndStopN">
		<bind role="onEnd"  component="lua"/>
		<bind role="stop" 	component="audio1"/>
		<bind role="stop"   component="audio2"/>		
		<bind role="stop"   component="audio3"/>		
		<bind role="stop"   component="audio4"/>		
		<bind role="stop"   component="audio5"/>		
		<bind role="stop"   component="audio6"/>		
		<bind role="stop"   component="audio7"/>		
		<bind role="stop"   component="audio8"/>		
		<bind role="stop"   component="audio13"/>	
		<bind role="stop"   component="image"/>		
	</link>

</body>
</ncl>