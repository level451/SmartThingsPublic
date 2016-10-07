import grails.converters.JSON

/**
*  JSON API Access App
*/

definition(
  name: "level451 bridge",
  namespace: "level451",
  author: "Todd Witzel",
  description: "Smartthings - level451 Interface",
  category: "My Apps",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
  oauth: [displayName: "level451", displayLink: "The level451 bridge"]
)


preferences {
  
  section("Control these Smartthings devices") {
    input "sensors", "capability.sensor", title: "Sensors", multiple: true, required: false
    input "actuators", "capability.actuator", title: "Actuator", multiple: true, required: false
    input "musicPlayers", "capability.musicPlayer", title: "MusicPlayers", multiple: true, required: false
	input "mediaController", "capability.mediaController", title: "Media Controller", required: false, multiple: true


  }
   section("Level451 Bridge Infomation") {
           input "ipaddress", "string", required: true, title: "Ipaddress:port"
    }
}

mappings {
  path("/updateip"){
    action: [
      GET: "updateip"
    ]
  }

  path("/things") {
    action: [
      GET: "listThings"
    ]
  }
    path("/createchild") {
        action: [
            GET: "createchild"
        ]
    }
        path("/update") {
        action: [
            GET: "doupdate"
        ]
    }
      path("/test") {
        action: [
            GET: "calltest"
        ]
    }
     path("/control") {
        action: [
            GET: "docontrol"
        ]
    }
   //test add
   path("/devices") {
		action: [
			GET: "listDevices"
	    ]
	}
}
def updated() {
    unsubscribe()
    initialize()
}
def initialize() {
    state.ipadress = ""
   log.debug "Installed with settings: ${settings}"
   //subscribe(actuators,"",switchHandler)
   //subscribe(sensors,"",switchHandler)
   subscribe(actuators,"power",switchHandler)
   subscribe(sensors,"power",switchHandler)
   subscribe(actuators,"switch",switchHandler)
   subscribe(sensors,"switch",switchHandler)
   subscribe(actuators,"level",switchHandler)
   subscribe(sensors,"level",switchHandler)
   subscribe(sensors, "temperature", switchHandler)
   subscribe(sensors, "heatingSetpoint", switchHandler)
   subscribe(sensors, "coolingSetpoint", switchHandler)
   subscribe(sensors, "thermostatSetpoint", switchHandler)
   subscribe(sensors, "thermostatMode", switchHandler)
   subscribe(sensors, "thermostatFanMode", switchHandler)
   subscribe(sensors, "thermostatOperatingState", switchHandler)
   //subscribe(sensors, "", switchHandler)
   //subscribe(sensors, "", switchHandler)

//subscribe(musicPlayers, "status", switchHandler)
	//subscribe(musicPlayers, "level", switchHandler)
	//subscribe(musicPlayers, "trackDescription", switchHandler)
	//subscribe(musicPlayers, "trackData", switchHandler)
	//subscribe(musicPlayers, "mute", switchHandler)
	//subscribe(mediaController, "activities", switchHandler)
   	//subscribe(mediaControllerlayer, "currentActivity", switchHandler)
   
   
  
  
}
def createchild(){
	//addChildDevice(request.JSON.namespace,request.JSON.name,request.JSON.networkid)
   
   
   // def x = addChildDevice("level451","Virtual Temp","2323")
   def name = request.JSON.name
    log.debug("name from json:$name")
  //  settings.devices.each {deviceId ->
     //   try {
            def existingDevice = getChildDevice(request.JSON.nid)
            if(!existingDevice) {
                def x = addChildDevice(request.JSON.nameSpace, request.JSON.type, request.JSON.nid, null, [name: "${name}", label: request.JSON.label , completedSetup: true])

            	log.debug ("Device added ID: $x.id");
                return [type:'addchilddevice',newdevice:true,id:x.id,nid:x.deviceNetworkId]
            } else
            {
            log.debug ("existing Device $existingDevice.name : $existingDevice.id");
            return [type:'addchilddevice',newdevice:false,id:existingDevice.id,nid:existingDevice.deviceNetworkId]
            }


}
def updateip(){

state.ipaddress = request.JSON.ip
updated()
log.debug "Ipaddress updated I hope $ipaddess"
return [success:"yes",ip:state.ipaddress]

}
def docontrol(){
control(request.JSON.id)
return [ok:"ok"]
}


def control(id) {
    def command = request.JSON.command
    def device = actuators.find { it.id == id}
    if (!device) {device =musicPlayers.find { it.id == id}}
	if (!device) {device =sensors.find { it.id == id}}
    if (!device) {device =getAllChildDevices().find { it.id ==id}}
 	log.debug "control, id:${id} name:${device.name}"
    //let's create a toggle option here
    if (command){
           log.debug(command)

       if (!device) {
            httpError(404, "Device not found")
        } else {
        
        // test lines
      log.debug 'req val:'+  request.JSON.value
       if (!request.JSON.value ){
        log.debug 'testtesttest' 
      device."$command"()
       }else
       {
       device."$command"(request.JSON.value)
       }
       
       device."$command"(request.JSON.value)
        return
        if (command == "Switch"){
         	log.debug "Switch Command from level451 value:"+request.JSON.value+ " ID:"+device.label
            	if (request.JSON.value > 0){
                    log.debug("device.on()")
								 device.on();
                    //device.turnon();
                }else{
                	log.debug("device.off()")
                   // device.turnoff();
                    device.off();
                
                
                }
                
         } else
            if (command == "setlevel"){
           		 log.debug 'setlevel - update vantage light '+request.JSON.value
                 if (request.JSON.value == 0){
                  device.turnoff()
                  device.setvalue(request.JSON.value)  

          		  }
                	else
          		  {
					device.turnon(request.JSON.value)
                    device.setvalue(request.JSON.value)  
         		   }
            
            
           	  }
            else if(command == "toggle")
            {
                log.debug 'toggle'
               if(device.currentValue('switch') == "on")
                  device.off();
                else
                  device.on();
            }
            else
            {
              if(request.JSON.value){
              log.debug("setting value")
           device.on()
           device.setvalue(request.JSON.value)  
               }
            }
        }
    }
    
}
def doupdate(){
update(request.JSON.id)
return [ok:"ok"]
}

def update(id) {
    def command = request.JSON.command
    def device = actuators.find { it.id == id}
    if (!device) {device =musicPlayers.find { it.id == id}}
	if (!device) {device =sensors.find { it.id == id}}
    if (!device) {device =getAllChildDevices().find { it.id ==id}}
 	log.debug "update, id:${id} name:${device.name}"
    //let's create a toggle option here
    if (command){
           log.debug(command)

       if (!device) {
            httpError(404, "Device not found")
        } else {
         if (command == "Switch"){
         	log.debug "Switch Command from level451 value:"+request.JSON.value+ " ID:"+device.label
            	if (request.JSON.value > 0){
                    log.debug("device.on()")
								 device.on();
                    //device.turnon();
                }else{
                	log.debug("device.off()")
                   // device.turnoff();
                    device.off();
                
                
                }
                
         } else
            if (command == "setlevel"){
           		 log.debug 'setlevel - update vantage light '+request.JSON.value
                 if (request.JSON.value == 0){
                  device.turnoff()
                  device.setvalue(request.JSON.value)  

          		  }
                	else
          		  {
					device.turnon(request.JSON.value)
                    device.setvalue(request.JSON.value)  
         		   }
            
            
           	  }
            else if(command == "toggle")
            {
                log.debug 'toggle'
               if(device.currentValue('switch') == "on")
                  device.off();
                else
                  device.on();
            }
            else
            {
              if(request.JSON.value){
              log.debug("setting value")
           device.on()
           device.setvalue(request.JSON.value)  
               }
            }
        }
    }
    
}



//  children: getAllChildDevices().collect{device(it,"")},


def	listThings() {
  [
  devices: sensors.collect{device(it,"sensor")}+actuators.collect{device(it,"actuator")},
  children: getAllChildDevices().collect{device(it,"")}
  ]
}
// test stuff *********************************************************
void calltest() {
log.debug "id "+request.JSON?.id
def id = request.JSON?.id
     def device = actuators.find { it.id == id}
    if (!device) {device =sensors.find { it.id == id}}
    if (!device) {device =getAllChildDevices().find { it.id ==id}}
 //def device = devices.find { it.id == id }
 log.debug "device:"+device
                if(device.currentValue('switch') == "on")
                  device.off();
                else
                  device.on();
}
private void test(devices){

def command = request.JSON?.command
log.debug "commandxx "+command
}


def switchHandler(evt) {

log.debug "Todds eventhandler: $evt.value,$evt.name,$evt.isStateChange ,$evt.device"

log.debug "ipaddess $state.ipaddress"
sendHubCommand(new physicalgraph.device.HubAction([
    method: "POST",
    path: "/api",
    headers: [
        HOST:state.ipaddress
        ],
        body:[source:"$evt.source",
        id:"$evt.deviceId",
        device:"$evt.displayName",
        name: "$evt.name",
        value: "$evt.value",
        data:"$evt.data",
        source:"$evt.source"
        
        ]]))
        
}
/**********************************

Child Calls


*********************/
def sendtolevel451(invalue,dev){
log.debug "send from child:"
  sendHubCommand(new physicalgraph.device.HubAction([
	method: "POST",
    path: "/api",
    headers: [
    	HOST:state.ipaddress
        ],
        body:[temp:invalue,id:dev.id]]))
}
def docommand(dev,cmd,value){
	log.debug "here" // never seems to happen ST bug - but the next command runs
    sendHubCommand(new physicalgraph.device.HubAction([
	method: "POST",
    path: "/api",
    headers: [
    	HOST:state.ipaddress
        ],
        body:[command:cmd,value:value,id:dev.id]]))
}
def manualRefresh(dev){
log.debug "manual refresh parent"
}
////////////////////// all test code below
def listDevices() {
	log.debug "getDevices, params: ${params}"
 [
  devices: sensors.collect{deviceItem(it)}
  //devices: sensors.collect{device(it,"sensor")}+actuators.collect{device(it,"actuator")},
  //children: getAllChildDevices().collect{device(it,"")}
  ]
//	allDevices.collect {
//		deviceItem(it)
//	}
}
private deviceItem(device) {
//https://searchcode.com/codesearch/view/89518190/
	def caps = device.capabilities

    [
		id: device.id,
        stid : device.id,
        type: "Smartthings",
		label: device.displayName,
        name: device.name,
        typename:device.typeName,
        author:device.typeAuthor,
//currentStates: device.currentStates,
		capabilities: device.capabilities?.collect {[
			name: it.name
		]},
		events: device.supportedAttributes?.collect {[
			name: it.name,
			dataType: it.dataType,
			values: it.values
		]},
		commands: device.supportedCommands?.collect {[
			name: it.name,
            command: it.name,
            arguments: it.arguments?it.arguments:null 
		]}
	
	]
}
def getDeviceCommands(){
//https://raw.githubusercontent.com/bravenel/SmartThings/aca858e1820b67bb076b02814713321e4a659276/Rule%20Machine%20X
	def result = ""
	devices.each { device ->
        result = device.supportedCommands.collect{ it as String }
        //log.debug "supportedCommands:${result}"
	}
	return result
}



//***************************************************************************************************************


private device(it, type) {
  def device_state = [name:it.name, label:it.label, type:type, id:it.id, stid:it.id]


def supportedCommands = it.supportedCommands

// logs each command's arguments
	device_state.commands = []
    	device_state.args = []

supportedCommands.each {
	
      // device_state.args=device_state.args+it

    def xargs = it.arguments
    	xargs.each{
            device_state.args=device_state.args+[atrname:it]
        
        }
        device_state.commands = device_state.commands + [name:it.name, sendto:"smartthings",command:it.name,args:device_state.args]

    
	}

  // log.debug "arguments for swithLevel command ${it.name}: ${it.arguments}"




// log each capability supported by the "mySwitch" device
	//device_state.commands = []
def mySwitchCaps = it.capabilities
//mySwitchCaps.each {cap ->
//     cap.commands.each {comm ->

 //       	device_state.commands = device_state.commands + [name:comm.name, sendto:"smartthings",command:comm.name]

   //     }
//}
device_state.events = []




for (attribute in it.supportedAttributes) {
  

  def currentState = it.currentState("${attribute}")
 device_state.events = device_state.events +[event:"${attribute}"]
 

	// this line crashes the system with the ecobee
    //device_state."${attribute}" = it.currentValue(currentValue)
  }
	device_state.capabilities = []
	def supportedCaps = it.capabilities
	supportedCaps.each {cap ->
    	device_state.capabilities = device_state.capabilities + cap.name
	}
    
 device_state ? device_state : null
}
 // "currentStates": [
//                {
                  //  "id": "95946690-8b89-11e6-b179-22000b6d8030",
                //    "hubId": "1edcb5db-2750-47e3-b273-98e5407fc25e",
              //      "isVirtualHub": false,
            //        "description": "Water Heater switch is on",
          //          "rawDescription": "zw device: 03, command: 2001, payload: FF",
        //            "displayed": true,
      //              "isStateChange": true,
    //                "linkText": "Water Heater",
  //                  "date": "2016-10-06T05:56:11.731Z",
//                    "unixTime": 1475733371731,
                //    "value": "on",
              //      "viewed": false,
            //        "translatable": false,
          //          "archivable": true,
        //            "deviceId": "0f0a636c-721b-4bc2-b4ba-caeaad9bce78",
      //              "name": "switch",
    //                "locationId": "f7fd780e-ac08-48fc-8bbf-50833e2092fc",
  //                  "eventSource": "DEVICE",
//                    "deviceTypeId": "5c2c78a2-b612-4fa4-919f-2f5466848047",
    //                "type": "physical",
  //                  "data": "{\"microDeviceTile\":{\"type\":\"standard\",\"icon\":\"st.Home.home2\",\"backgroundColor\":\"#79b821\"}}"
//                },