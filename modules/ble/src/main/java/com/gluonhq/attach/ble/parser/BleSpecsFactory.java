package com.gluonhq.attach.ble.parser;

import com.gluonhq.attach.ble.BleSpecs.GattCharacteristics;
import com.gluonhq.attach.ble.BleSpecs.GattDescriptors;

public class BleSpecsFactory {
    
    public static BleParser getCharacteristicsParser(GattCharacteristics gatt) {
        switch (gatt) {
            case AEROBIC_HEART_RATE_LOWER_LIMIT:
            case AEROBIC_HEART_RATE_UPPER_LIMIT:
            case AEROBIC_THRESHOLD:
            case ANAEROBIC_HEART_RATE_LOWER_LIMIT:
            case ANAEROBIC_HEART_RATE_UPPER_LIMIT:
            case ANAEROBIC_THRESHOLD:
            case FAT_BURN_HEART_RATE_LOWER_LIMIT:
            case FAT_BURN_HEART_RATE_UPPER_LIMIT:  
            case HEART_RATE_MAX:
            case MAXIMUM_RECOMMENDED_HEART_RATE:
            case RESTING_HEART_RATE:
                return new HeartRateBleParser();
            case APPEARANCE: 
                return new AppearanceBleParser();
            case DEVICE_NAME:
            case EMAIL_ADDRESS:
            case FIRMWARE_REVISION_STRING:
            case FIRST_NAME:
            case HARDWARE_REVISION_STRING:
            case HTTP_ENTITY_BODY:
            case HTTP_HEADERS: 
            case LANGUAGE:
            case LAST_NAME:
            case LOCATION_NAME:
            case MANUFACTURER_NAME_STRING:
            case MODEL_NUMBER_STRING:
            case OBJECT_NAME:
            case SERIAL_NUMBER_STRING:
            case SOFTWARE_REVISION_STRING:
            case URI:
                return new StringBleParser();
            case HEART_RATE_MEASUREMENT:
                return new HeartRateMeasurementBleParser();
            case PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS: 
                return new PeripheralPreferredBleParser();
        }
        return new DefaultBleParser();
    }
    
    public static BleParser getDescriptorParser(GattDescriptors gatt) {
        switch (gatt) {
            case CHARACTERISTIC_USER_DESCRIPTION: return new StringBleParser();
            case CLIENT_CHARACTERISTIC_CONFIGURATION: return new ClientConfigurationBleParser();
        }
        return new DefaultBleParser();
    }
    
}
