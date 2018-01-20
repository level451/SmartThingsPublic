/**
 *  Level451 Device FInder
 *
 *  Copyright 2015 Todd Witzel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Level451 Device Finder",
    namespace: "level451",
    author: "Todd Witzel",
    description: "Finds Level451 Devices on the local network",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
 
 
preferences {
	page(name:"level451Discover", title:"Level451 Device FInder", content:"level451Discovery", refreshTimeout:5)
    page(name:"installOptions", title:"installOptions", content:"installOptions", refreshTimeout:5)
}
mappings {
  path("/sample") {
    action: [
      GET: "listMethod",
      PUT: "updateMethod"
    ]
  }
}


def updateMethod(evt){
log.debug("put")
}
def listMethod(evt){
log.debug("get")
}
 
def level451Discovery()
{
	log.debug("page render")
    
		int sonosRefreshCount = !state.sonosRefreshCount ? 0 : state.sonosRefreshCount as int
		state.sonosRefreshCount = sonosRefreshCount + 1
        log.debug("resheshcount"+sonosRefreshCount)
		def refreshInterval = 3
// 
		def options = level451hubsDiscovered() ?: []
//
		def numFound = options.size() ?: 0
 
		
        if(!state.subscribe) {
			log.trace "subscribe to location"
			subscribe(location, null, level451locationHandler, [filterEvents:false])
			state.subscribe = true
		}
		//sonos discovery request every 5 //25 seconds
		if((sonosRefreshCount % 2) == 0) {
			discover()
	}

		//setup.xml request every 3 seconds except on discoveries
//		if(((sonosRefreshCount % 1) == 0) && ((sonosRefreshCount % 8) != 0)) {
//			verifySonosPlayer()
//		}
		log.debug "options"
        log.debug options
        return dynamicPage(name:"level451Discover", title:"Discovery Started!", nextPage:"installOptions", refreshInterval:refreshInterval, install:false, uninstall: true) {
			section("2Please wait while we discover your Sonos. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
				input "selectedHub", "enum", required:true, title:"Select Sonos (${numFound} found)", multiple:false, options:options
			}
		}
}
def installOptions()
{
		log.debug("installOptions page render")
        log.debug(selectedHub)
		def refreshInterval = 4
        
        if (!state.body){
        log.debug("requesting capabilities") 
    	
        log.trace devices."${selectedHub.toString()}"
        String deviceNetworkId = (devices."${selectedHub.toString()}".ip + ":" + devices."${selectedHub.toString()}".port)
        log.trace(deviceNetworkId)
        
        String ip = getHostAddress(deviceNetworkId)
        log.trace "ip:" + ip
        state.ip = ip

      	
        sendHubCommand(new physicalgraph.device.HubAction("""GET /xml/device_description.xml HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${$deviceNetworkID}"))
        
        return dynamicPage(name:"installOptions", title:"What to install", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: false) {
			section("Seeing what your wonderful level451 hub can do - please wait") 
        }
        } else {
       log.debug(state.body)
       log.debug(state.body.options)
       return dynamicPage(name:"installOptions", title:"What to install", nextPage:"", refreshInterval:0, install:true, uninstall: false) {
			section("Select Capabilities to install") {
				input "installOptions", "enum", required:false, title:"Select options)", multiple:true, options:state.body.options
		
        }
        
        
        	}
		}


}
	
def installed() {
	log.debug "Installed with settings: ${settings}"
	
	unsubscribe()
	state.subscribe = false
	//initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	state.subscribe = false
    initialize()
}
def discover() {
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:level451.com:device:*", physicalgraph.device.Protocol.LAN))
    
}
def initialize() {
	state.body = ""
    state.sonosRefreshCount = 0
    String deviceNetworkId = (devices."${selectedHub.toString()}".ip + ":" + devices."${selectedHub.toString()}".port)
    log.trace(deviceNetworkId)
    installOptions.each { devicetoinstall ->
		def dni = deviceNetworkId+'/'+devicetoinstall
        log.trace dni
        def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice("level451", "switch", dni, devices."${selectedHub.toString()}".hub, [label:"$devicetoinstall level451"])
			log.trace "created $devicetoinstall with id $dni"
			
		//	d.setModel(newPlayer?.value.model)
		//	log.trace "setModel to ${newPlayer?.value.model}"
//
//			runSubscribe = true
		} else {
			log.trace "found ${d.displayName} with id $dni already exists"
            d.state.test="asdf"
            log.debug(d)
            log.debug(d.state)
            log.debug("done d")
		}
	}
    
    
    
// TODO: subscribe to attributes, devices, locations, etc.
}

def level451locationHandler(evt) {
  def description = evt.description
  def hub = evt?.hubId
  log.debug("location handler fired");
  def parsedEvent = parseEventMessage(description)
 // log.debug(parsedEvent)
 // log.debug(parsedEvent.mac)
// I think this is adding the hub to the parsed event
parsedEvent << ["hub":hub]

 if (parsedEvent?.ssdpPath?.contains("rest"))
 {

    def devices = getDevices()

    if (!(devices."${parsedEvent.mac.toString()}"))
    {
      devices << ["${parsedEvent.mac.toString()}":parsedEvent]
   	  log.debug("device added to state")  	
    } else
    {
    	log.debug("device already know")
        log.debug(devices.$)
        
    }
   
        
}

///////**((((
else if (parsedEvent.headers && parsedEvent.body)
	{ // SONOS RESPONSES 
		
        
        log.debug "evt object: ${evt.inspect()}"
        
        def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())

		def type = (headerString =~ /Content-Type:.*/) ? (headerString =~ /Content-Type:.*/)[0] : null
		def body
		log.debug "level451 REPONSE: ${headerString.split('\n').findAll{it.trim()}.join(', ')}, TYPE: ${type}, BODY: ${bodyString.size()} BYTES"
		if(type?.contains("json"))
		{ //(application/json)
        	log.trace "Is JSON"
			body = new groovy.json.JsonSlurper().parseText(bodyString)
			log.trace "GOT JSON $body"
			log.trace (state.devices)
         	state.body = body   
    }
		else {
        	log.grace "Is not JSON"
        }
	}
	else {
		 log.debug "evt object: ${evt.inspect()}"
        def headerString = new String(parsedEvent.headers.decodeBase64())
        def type = headerString.split(" ")[0]
        def path = headerString.split(" ")[1].substring(1)
        //log.trace " desc:" + headerString.split(" ")[1]
        log.trace "type:"+type
		log.trace "path:"+path
		 def children = getAllChildDevices()
            log.trace("children"+children)
            children.each {
            log.trace("child device"+it)
            }
       
     //   console.log(state.getProperties())
        def dw = getChildDevice(path)
        log.debug("fget child device:"+dw)
		log.debug(evt.getProperties())        					     
        if (dw) {
        log.debug("child found")
        }
		//log.trace description
	}

////////////******************
}
def getDevices()
{
  if (!state.devices) { state.devices = [:] }
  state.devices
}
private def parseEventMessage(String description) {
	def event = [:]
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('devicetype:')) {
			def valueString = part.split(":")[1].trim()
			event.devicetype = valueString
		}
		else if (part.startsWith('mac:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.mac = valueString
			}
		}
		else if (part.startsWith('networkAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ip = valueString
			}
		}
		else if (part.startsWith('deviceAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.port = valueString
			}
		}
		else if (part.startsWith('ssdpPath:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ssdpPath = valueString
			}
		}
		else if (part.startsWith('ssdpUSN:')) {
			part -= "ssdpUSN:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpUSN = valueString
			}
		}
		else if (part.startsWith('ssdpTerm:')) {
			part -= "ssdpTerm:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpTerm = valueString
			}
		}
		else if (part.startsWith('headers')) {
			part -= "headers:"
			def valueString = part.trim()
			if (valueString) {
				event.headers = valueString
			}
		}
		else if (part.startsWith('body')) {
			part -= "body:"
			def valueString = part.trim()
			if (valueString) {
				event.body = valueString
			}
		}
	}

	event
}

private getHostAddress(d) {
	def parts = d.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}
private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}
Map level451hubsDiscovered() {
	def devices = state.devices
	def map = [:]
	devices.each {
		def value = "${it.value.ssdpPath}"
		def key = it.value.mac
		map["${key}"] = value
	}
	log.trace("MAP:"+map)

    map
}
def teston(dev){
	log.debug("parent on command")
    log.debug(dev)
    log.debug("requesting capabilities") 
    	
        log.trace devices."${selectedHub.toString()}"
        String deviceNetworkId = (devices."${selectedHub.toString()}".ip + ":" + devices."${selectedHub.toString()}".port)
        log.trace(deviceNetworkId)
        
        String ip = getHostAddress(deviceNetworkId)
        log.trace "ip:" + ip

      	
        sendHubCommand(new physicalgraph.device.HubAction("""GET /xml/$dev HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${$deviceNetworkID}"))
   
}
def off(dev){
	log.debug("parent off command")
}