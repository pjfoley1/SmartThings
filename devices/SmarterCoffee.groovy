/**
 *  Smarter Coffee
 *
 *  Copyright 2017 Peter Major
 *
 *  Version 1.0.0   17 Apr 2017		Initial release
 *  Version 1.1.0   22 Apr 2017		Notify user when start brew fails
 *
 *	Version History
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
	definition (name: "Smarter Coffee", namespace: "petermajor", author: "Peter Major") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Switch"

		command "changeCups"
		command "changeStrength"
		command "changeGrind"
		command "changeHotplate"
	}

	tiles(scale: 2) {

		standardTile("switch", "device.switch", width: 6, height: 4, canChangeIcon: true, decoration: "flat") {
			state "off", label: 'off', action: "switch.on", icon: "st.Appliances.appliances14", backgroundColor: "#ffffff", nextState:"on"
			state "on", label: 'on', action: "switch.off", icon: "st.Appliances.appliances14", backgroundColor: "#00a0dc", nextState:"off"
		}

		standardTile("cups", "device.cups", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "1", label:'1\ncup', action: "changeCups", nextState:"2"
			state "2", label:'2\ncups', action: "changeCups", nextState:"3"
			state "3", label:'3\ncups', action: "changeCups", nextState:"4"
			state "4", label:'4\ncups', action: "changeCups", nextState:"5"
			state "5", label:'5\ncups', action: "changeCups", nextState:"6"
			state "6", label:'6\ncups', action: "changeCups", nextState:"7"
			state "7", label:'7\ncups', action: "changeCups", nextState:"8"
			state "8", label:'8\ncups', action: "changeCups", nextState:"9"
			state "9", label:'9\ncups', action: "changeCups", nextState:"10"
			state "10", label:'10\ncups', action: "changeCups", nextState:"11"
			state "11", label:'11\ncups', action: "changeCups", nextState:"12"
			state "12", label:'12\ncups', action: "changeCups", nextState:"1"
		}

		standardTile("strength", "device.strength", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "0", label: 'weak\nstrength', action: "changeStrength", nextState:"1"
			state "1", label: 'medium\nstrength', action: "changeStrength", nextState:"2"
			state "2", label: 'strong\nstrength', action: "changeStrength", nextState:"0"
		}

		standardTile("isGrind", "device.isGrind", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "true", label: 'grind\nmode', action: "changeGrind", nextState:"false"
			state "false", label: 'filter\nmode', action: "changeGrind", nextState:"true"
		}

		standardTile("isHotplate", "device.isHotplate", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "true", label: 'heater\non', backgroundColor: "#00a0dc", action: "changeHotplate", nextState:"false"
			state "false", label: 'heater\noff', backgroundColor: "#ffffff", action: "changeHotplate", nextState:"true"
		}

		valueTile("waterLevel", "device.waterLevel", inactiveLabel: false, width: 2, height: 2) {
			state "0", label: 'water\nempty', backgroundColor: "#ff0000" 
			state "1", label: 'water\nlow', backgroundColor: "#ff0000" 
			state "2", label: 'water\nhalf full', backgroundColor: "#ffffff" 
			state "3", label: 'water\nfull', backgroundColor: "#ffffff" 
		}

		valueTile("isCarafeDetected", "device.isCarafeDetected", inactiveLabel: false, width: 2, height: 2) {
			state "true", label: 'carafe', backgroundColor: "#ffffff" 
			state "false", label: 'no\ncarafe', backgroundColor: "#ff0000"
		}

		standardTile("refresh", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details (["switch", "cups", "strength", "isGrind", "isHotplate", "waterLevel", "isCarafeDetected", "refresh"])
	}
}

def parse(description) {

 	log.debug "SmarterCoffee Parse ${description}"

}

def installed() {
	// on install the first status seems to be too early
	// so delay it a couple of secs
	runIn(2, initialize)
}

def updated() {
}

def initialize() {

	log.debug "SmarterCoffee Initialize"

	def action = getStatus()
	sendHubCommand(action)
}

def poll() {
	log.debug "SmarterCoffee Poll"
	getStatus()
}

def refresh() {
	log.debug "SmarterCoffee Refresh"
	getStatus()
}

def on() {
	log.debug "SmarterCoffee On"

	int currentCups = Integer.parseInt(device.currentValue("cups"))
	int currentStrength = Integer.parseInt(device.currentValue("strength"))
	Boolean currentIsGrind = new Boolean(device.currentValue("isGrind"))

	def payload = [
		isGrind: currentIsGrind, 
		cups: currentCups, 
		strength: currentStrength]

	sendEvent(name: "switch", value: "on")

	return doPost("brew/on", payload)
}

def off() {
	log.debug "SmarterCoffee Off"

	sendEvent(name: "switch", value: "off")

	return doPost("brew/off")
}

def changeCups() {

	log.debug "SmarterCoffee changeCups"

	int currentCups = Integer.parseInt(device.currentValue("cups"))
	int newCups = currentCups >= 12 ? 1 : currentCups + 1;

	def payload = [cups: newCups]

	sendEvent(name: "cups", value: newCups.toString())

	return doPost("cups", payload)
}

def changeStrength() {

	log.debug "SmarterCoffee changeStrength"

	int currentStrength = Integer.parseInt(device.currentValue("strength"))
	int newStrength = currentStrength >= 2 ? 0 : currentStrength + 1;

	def payload = [strength: newStrength]

	sendEvent(name: "strength", value: newStrength.toString())

	return doPost("strength", payload)
}

def changeGrind() {

	log.debug "SmarterCoffee changeGrind"

	Boolean currentIsGrind = new Boolean(device.currentValue("isGrind"))
	Boolean newIsGrind = !currentIsGrind
	
	def payload = [isGrind: newIsGrind]

	sendEvent(name: "isGrind", value: newIsGrind.toString())

	return doPost("grind", payload)
}

def changeHotplate() {

	log.debug "changeHotplate"

	Boolean currentIsHotplate = new Boolean(device.currentValue("isHotplate"))
	Boolean newIsHotplate = !currentIsHotplate
	
	log.debug "changeGrind $currentIsHotplate $newIsHotplate"

	def payload = newIsHotplate ? [mins: 5] : null
	def path = newIsHotplate ? "hotplate/on" : "hotplate/off"

	sendEvent(name: "isHotplate", value: newIsHotplate.toString())

	return doPost(path, payload)
}

def getStatus() {
	def host = getHostAddress()
	if (!host)
		return 

	def p = [
		method: "GET", 
		path: getDevicePath(), 
		headers: [
			HOST: host, 
			Accept: "application/json"]]

	def dni = device.deviceNetworkId
	def o = [callback: getStatusCallback]
    def action = new physicalgraph.device.HubAction(p, dni, o)

	log.debug "SmarterCoffee getStatusAction ${action}"
	return action
}

void getStatusCallback(physicalgraph.device.HubResponse hubResponse) {

	log.debug "getStatusCallback ${hubResponse}"

	// TODO - errors?
	if (hubResponse.status != 200) return

	def body = hubResponse.json
	log.debug "getStatusCallback ${body}"

	def status = body?.status

	if (status) {
		updateStatus(status)
	}

	if (!getDataValue("subscriptionId")) {
		doSubscribe();
	}

}

void updateStatus(status) {

	log.debug "updateStatus ${status}"

	sendEvent(name: "switch", value: status.isBrewing ? "on" : "off")
	sendEvent(name: "cups", value: status.cups.toString())	
	sendEvent(name: "strength", value: status.strength.toString())
	sendEvent(name: "isGrind", value: status.isGrind.toString())
	sendEvent(name: "isHotplate", value: status.isHotplateOn.toString())
	sendEvent(name: "waterLevel", value: status.waterLevel.toString())
	sendEvent(name: "isCarafeDetected", value: status.isCarafeDetected.toString())
}

void notifyError(error) {

	log.debug "notifyError ${error}"

	def action = getStatus()
	sendHubCommand(action)
}

def doSubscribe() {

	def host = getHostAddress()
	def fullPath = getDevicePath()
	def hub = getLocalHubAddress()

	def params = [
		method: 'SUBSCRIBE',
		path: fullPath,
		headers: [
			HOST: host,
			CALLBACK: "<http://${hub}/smarter-coffee-callback>", 
			Accept: 'application/json',
			TIMEOUT: 'Second-360']]

	def sid = getDataValue("subscriptionId")
	if (sid) {
		params.headers << [SID: sid]
	}

	def o = [callback: subscribeCallback]

	def dni = device.deviceNetworkId

	def action = new physicalgraph.device.HubAction(params, dni, o)

	log.debug "SmarterCoffee doSubscribe ${action}"

	// subscription expires in 6 min, 
	// so resubscribe in 5 mins just to be safe
	runIn(5*60, doSubscribe)

	sendHubCommand(action)
}

void subscribeCallback(physicalgraph.device.HubResponse hubResponse) {

	log.debug "subscribeCallback ${hubResponse}"

	if (hubResponse.status != 200) return

	def sid = hubResponse.headers["SID"];

	log.debug "subscribeCallback SID ${sid}"

	updateDataValue("subscriptionId", sid)
}

def doPost(String path, Map data) {

	def host = getHostAddress()
	def fullPath = getDevicePath(path)

	def params = [
		method: "POST",
		path: fullPath,
		body: data,
		headers: [HOST: host]]

	def action = new physicalgraph.device.HubAction(params, device.deviceNetworkId)

	log.debug "SmarterCoffee doPost ${action}"

	return action
}

def sync(serverAddress, serverPort, serverMac, deviceId) {
	log.debug "SmarterCoffee Sync $serverAddress $serverPort $serverMac $deviceId"

	def serverAddressOld = getDataValue("serverAddress")
	if (serverAddress && serverAddress != serverAddressOld) {
		updateDataValue("serverAddress", serverAddress)
	}
	def serverPortOld = getDataValue("serverPort")
	if (serverPort && serverPort != serverPortOld) {
		updateDataValue("serverPort", serverPort)
	}
	def serverMacOld = getDataValue("serverMac")
	if (serverMac && serverMac != serverMacOld) {
		updateDataValue("serverMac", serverMac)
	}
	def deviceIdOld = getDataValue("deviceId")
	if (deviceId && deviceId != deviceIdOld) {
		updateDataValue("deviceId", deviceId)
	}
}

def getHostAddress() {
	def serverAddress = getDataValue("serverAddress")
	def serverPort = getDataValue("serverPort")
	return (serverAddress && serverPort) ? "$serverAddress:$serverPort" : null
}

def getLocalHubAddress() {
	def localIp = device.hub.getDataValue("localIP");
	def localSvcPort = device.hub.getDataValue("localSrvPortTCP");
	return (localIp && localSvcPort) ? "$localIp:$localSvcPort" : null
}

def getDevicePath(path) {
	def deviceId = getDataValue("deviceId")
	def basePath = "/api/device/$deviceId"

	return (path!=null && path.length()>0) ? "$basePath/$path" : basePath
}