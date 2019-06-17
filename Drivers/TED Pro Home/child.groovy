/*
 * TED Pro Home Spyder
 *
 *  Copyright 2019 Daniel Terryn
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
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2019-05-21  Daniel Terryn  Original Creation
 * 
 */

metadata {
    definition (name: "TED Pro Home Spyder", author: "dan.t", namespace: "dan.t") {
        capability "Power Meter"
        capability "Sensor"
    }
}

def setLoggingLevel(newLoggingLevel)
{
    state.loggingLevelIDE = newLoggingLevel
}
def poll() {
    logger('Poll Called', "trace")
}

def refresh() {
    logger('Refresh Called', "trace")
}

def parse(String description) {
    logger("parse(${description}) called", "debug")
    def parts = description.split(" ")
    def name  = parts.length>0?parts[0].trim():null
    def value = parts.length>1?parts[1].trim():null
    if (name && value) {
        // Update device
        valueChangeEvent(name, value, "W")
    }
    else {
        logger("Missing either name or value.  Cannot parse!", "error")
    }
}


def valueChangeEvent(def deviceAttribute, def newValue, def newUnit)
{
    def success = false
    def oldValue = null

    def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)

    if (state?.data_points)
    {
        if (state?.data_points["${deviceAttribute}"])
        {
            oldValue = state?.data_points["${deviceAttribute}"]
            if (state?.data_points["${deviceAttribute}"] == newValue)
                return false
            else if (state?.data_points["${deviceAttribute}"].toString().equals(newValue.toString()))
                return false
        }
    }
    else
        state.data_points = [:]
    
    state.data_points["${deviceAttribute}"] = newValue
    
    logger("----> Send new ${deviceAttribute} state, old: ${oldValue}, new: ${newValue}", "debug")
    sendEvent(name: deviceAttribute, value: newValue, unit:  newUnit)
    
    return true;
}


def installed() {
    state.loggingLevelIDE = (state.loggingLevelIDE) ? state.loggingLevelIDE.toInteger() : 3
}

private logger(msg, level = "debug") {
    
    if (state.loggingLevelIDE == null)
        state.loggingLevelIDE = 3
    switch(level) {
        case "error":
            if (state.loggingLevelIDE >= 1) log.error msg
            break

        case "warn":
            if (state.loggingLevelIDE >= 2) log.warn msg
            break

        case "info":
            if (state.loggingLevelIDE >= 3) log.info msg
            break

        case "debug":
            if (state.loggingLevelIDE >= 4) log.debug msg
            break

        case "trace":
            if (state.loggingLevelIDE >= 5) log.trace msg
            break

        default:
            log.debug msg
            break
    }
}


