/**
 *  Virtual Momentary Switch
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
metadata {
	definition (name: "Vantage Momentary Button", namespace: "level451", author: "Todd Witzel") {
	capability "Momentary"
    capability "Switch"}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
		state "off", label: 'Push', action: "momentary.push", icon:"st.switches.light.on", backgroundColor: "#afffff", nextState: "on"
		state "on", label: 'Push', action: "momentary.push", icon:"st.switches.light.off", backgroundColor: "#53a7c0", nextState: "off"
        
        
	}
      main(["switch"])
}
}
def push() {
log.debug "push"
	parent.docommand(device,'push',0)
   // sendEvent(name: "switch", value: "on")
    //    sendEvent(name: "switch", value: "off")


}
def on() {
// echo doesn't support push - only on off events - 
log.debug "push - on"
push()
}
def off(){
log.debug "push - off"
push()
}
// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}

//state "on", label: 'Push', action: "momentary.push", icon:"st.switches.light.off", backgroundColor: "#53a7c0"