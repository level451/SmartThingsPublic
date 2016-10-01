import grails.converters.JSON
/**
*  JSON API Access App
*/

definition(
  name: "Theater bridge",
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
   section("Bridge Infomation") {
           input "ipaddress", "string", required: true, title: "Ipaddress:port"
    }
}

mappings {
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
}
def updated() {
    unsubscribe()
    initialize()
}
def initialize() {
    
   log.debug "Installed with settings: ${settings}"
   //subscribe(actuators,"",switchHandler)
   //subscribe(sensors,"",switchHandler)
   subscribe(actuators,"power",switchHandler)
   subscribe(sensors,"power",switchHandler)
   subscribe(actuators,"switch",switchHandler)
   subscribe(sensors,"switch",switchHandler)
   subscribe(actuators,"level",switchHandler)
   subscribe(sensors,"level",switchHandler)
   subscribe(sensors, "energy", switchHandler)
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
       // } catch (e) {
     //       log.error "Error creating device: ${e}"
     //   }
   // }

  //def x=actuators.find { it.id == request.JSON.id}

//	x.on()
//def x=getAllChildDevices().find { it.id == request.JSON.id}
//log.debug ("**json passed name for id is $x.name")	
//def	x=getChildDevice(request.JSON.nid)
//  if (x){
//  log.debug (x)
 // } else
 // {
 // log.debug("device not found?")
 // return
//  }
  
  
//  log.debug ("child device"+x)
//    if (request.JSON.temp){
 //   	x.setvalue(request.JSON.temp)
//   }
    //x.label = "aaa"
   
	
//    log.debug ("$x.id")
  //return [id:x.id]

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
        if (!device) {
            httpError(404, "Device not found")
        } else {
         if (command == "Switch"){
         	log.debug "Switch Command from level451 value:"+request.JSON.value+ " ID:"+device.label
            	if (request.JSON.value > 0){
                    log.debug("device.on()")

                    device.on();
                }else{
                	log.debug("device.off()")
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
                if(device.currentValue('switch') == "on")
                  device.off();
                else
                  device.on();
            }
            else
            if (command == "setcolor"){
                          log.debug("setcolor called ****")
                          log.debug(request.JSON.value)
                          //device.setcolor(request.JSON.value)
                          

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

private device(it, type) {
  def device_state = [name:it.name, label:it.label, type:type, id:it.id, stid:it.id]

  for (attribute in it.supportedAttributes) {
    device_state."${attribute}" = it.currentValue("${attribute}")
  }
	device_state.commands = []
    def supportedCaps = it.capabilities
	supportedCaps.each {cap ->
    	device_state.commands = device_state.commands + [name:cap.name, sendto:"smartthings"]
        log.debug "This device (${it}) supports the ${cap.name} capability"
	}
    
 device_state ? device_state : null
}
def switchHandler(evt) {

log.debug "Todds switch called: $evt.value,$evt.name,$evt.isStateChange ,$evt.device"


  

sendHubCommand(new physicalgraph.device.HubAction([
	method: "POST",
    path: "/api",
    headers: [
    	HOST:ipaddress
        ],
        body:[source:"$evt.source",id:"$evt.deviceId",device:"$evt.displayName",name: "$evt.name",value: "$evt.value",data:"$evt.data"]],"asdf"))
        
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
    	HOST:ipaddress
        ],
        body:[temp:invalue,id:dev.id]]))
}
def docommand(dev,cmd,value){
	log.debug "here" // never seems to happen ST bug - but the next command runs
    sendHubCommand(new physicalgraph.device.HubAction([
	method: "POST",
    path: "/api",
    headers: [
    	HOST:ipaddress
        ],
        body:[command:cmd,value:value,id:dev.id]]))
}
def manualRefresh(dev){
log.debug "manual refresh parent"
}