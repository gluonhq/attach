/*
 * Copyright (c) 2020, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.attach.ble;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * All BLE UUIDs are of the form: 0000XXXX-0000-1000-8000-00805f9b34fb
 * 
 * These specs specify the unique XXXX code for each of the currently existing 
 * Services, Characteristics and Descriptors
 * 
 * For more info about BLE see: 
 * https://www.bluetooth.com/specifications/generic-attributes-overview
 * 
 */
public class BleSpecs {

    private static final Logger LOG = Logger.getLogger(BleSpecs.class.getName());

    private static final String UUID_BASE_PREFIX = "0000";
    private static final String UUID_BASE_SUFIX = "-0000-1000-8000-00805f9b34fb";
    /**
     * https://www.bluetooth.com/specifications/gatt/services
     */
    public enum GattServices {
        ALERT_NOTIFICATION_SERVICE("Alert Notification Service", "org.bluetooth.service.alert_notification", 0x1811),
        AUTOMATION_IO("Automation IO", "org.bluetooth.service.automation_io", 0x1815),
        BATTERY_SERVICE("Battery Service", "org.bluetooth.service.battery_service", 0x180F),
        BLOOD_PRESSURE("Blood Pressure", "org.bluetooth.service.blood_pressure", 0x1810),
        BODY_COMPOSITION("Body Composition", "org.bluetooth.service.body_composition", 0x181B),
        BOND_MANAGEMENT("Bond Management", "org.bluetooth.service.bond_management", 0x181E),
        CONTINUOUS_GLUCOSE_MONITORING("Continuous Glucose Monitoring", "org.bluetooth.service.continuous_glucose_monitoring", 0x181F),
        CURRENT_TIME_SERVICE("Current Time Service", "org.bluetooth.service.current_time", 0x1805),
        CUSTOM_SERVICE("Custom Service", "", 0x0000),
        CYCLING_POWER("Cycling Power", "org.bluetooth.service.cycling_power", 0x1818),
        CYCLING_SPEED_AND_CADENCE("Cycling Speed and Cadence", "org.bluetooth.service.cycling_speed_and_cadence", 0x1816),
        DEVICE_INFORMATION("Device Information", "org.bluetooth.service.device_information", 0x180A),
        ENVIRONMENTAL_SENSING("Environmental Sensing", "org.bluetooth.service.environmental_sensing", 0x181A),
        FITNESS_MACHINE("Fitness Machine", "org.bluetooth.service.fitness_machine", 0x1826),
        GENERIC_ACCESS("Generic Access", "org.bluetooth.service.generic_access", 0x1800),
        GENERIC_ATTRIBUTE("Generic Attribute", "org.bluetooth.service.generic_attribute", 0x1801),
        GLUCOSE("Glucose", "org.bluetooth.service.glucose", 0x1808),
        HEALTH_THERMOMETER("Health Thermometer", "org.bluetooth.service.health_thermometer", 0x1809),
        HEART_RATE("Heart Rate", "org.bluetooth.service.heart_rate", 0x180D),
        HTTP_PROXY("HTTP Proxy", "org.bluetooth.service.http_proxy", 0x1823),
        HUMAN_INTERFACE_DEVICE("Human Interface Device", "org.bluetooth.service.human_interface_device", 0x1812),
        IMMEDIATE_ALERT("Immediate Alert", "org.bluetooth.service.immediate_alert", 0x1802),
        INDOOR_POSITIONING("Indoor Positioning", "org.bluetooth.service.indoor_positioning", 0x1821),
        INTERNET_PROTOCOL_SUPPORT("Internet Protocol Support", "org.bluetooth.service.internet_protocol_support", 0x1820),
        LINK_LOSS("Link Loss", "org.bluetooth.service.link_loss", 0x1803),
        LOCATION_AND_NAVIGATION("Location and Navigation", "org.bluetooth.service.location_and_navigation", 0x1819),
        NEXT_DST_CHANGE_SERVICE("Next DST Change Service", "org.bluetooth.service.next_dst_change", 0x1807),
        OBJECT_TRANSFER("Object Transfer", "org.bluetooth.service.object_transfer", 0x1825),
        PHONE_ALERT_STATUS_SERVICE("Phone Alert Status Service", "org.bluetooth.service.phone_alert_status", 0x180E),
        PULSE_OXIMETER("Pulse Oximeter", "org.bluetooth.service.pulse_oximeter", 0x1822),
        REFERENCE_TIME_UPDATE_SERVICE("Reference Time Update Service", "org.bluetooth.service.reference_time_update", 0x1806),
        RUNNING_SPEED_AND_CADENCE("Running Speed and Cadence", "org.bluetooth.service.running_speed_and_cadence", 0x1814),
        SCAN_PARAMETERS("Scan Parameters", "org.bluetooth.service.scan_parameters", 0x1813),
        TRANSPORT_DISCOVERY("Transport Discovery", "org.bluetooth.service.transport_discovery", 0x1824),
        TX_POWER("Tx Power", "org.bluetooth.service.tx_power", 0x1804),
        USER_DATA("User Data", "org.bluetooth.service.user_data", 0x181C),
        WEIGHT_SCALE("Weight Scale", "org.bluetooth.service.weight_scale", 0x181D);
        
        private final String specificationName;
        private final String specificationType;
        private final long assignedNumber;
        
        GattServices(String specificationName, String specificationType, long assignedNumber) {
            this.specificationName = specificationName;
            this.specificationType = specificationType;
            this.assignedNumber = assignedNumber;
        }

        public String getSpecificationName() {
            return specificationName;
        }

        public String getSpecificationType() {
            return specificationType;
        }

        public long getAssignedNumber() {
            return assignedNumber;
        }
        
        public static GattServices ofAssignedNumber(long number) {
            for (GattServices service : values()) {
                if (service.getAssignedNumber() == number) {
                    return service;
                }
            }
            return GattServices.CUSTOM_SERVICE;
        }
        
        public static GattServices ofAssignedName(String specificationName) {
            for (GattServices service : values()) {
                if (service.getSpecificationName().equals(specificationName) ||
                        service.getSpecificationName().equals(specificationName + " Service")) {
                    return service;
                }
            }
            return GattServices.CUSTOM_SERVICE;
        }
    }
    
    /**
     * https://www.bluetooth.com/specifications/gatt/characteristics
     */
    
    public enum GattCharacteristics {
        AEROBIC_HEART_RATE_LOWER_LIMIT("Aerobic Heart Rate Lower Limit", "org.bluetooth.characteristic.aerobic_heart_rate_lower_limit", 0x2A7E),
        AEROBIC_HEART_RATE_UPPER_LIMIT("Aerobic Heart Rate Upper Limit", "org.bluetooth.characteristic.aerobic_heart_rate_upper_limit", 0x2A84),
        AEROBIC_THRESHOLD("Aerobic Threshold", "org.bluetooth.characteristic.aerobic_threshold", 0x2A7F),
        AGE("Age", "org.bluetooth.characteristic.age", 0x2A80),
        AGGREGATE("Aggregate", "org.bluetooth.characteristic.aggregate", 0x2A5A),
        ALERT_CATEGORY_ID("Alert Category ID", "org.bluetooth.characteristic.alert_category_id", 0x2A43),
        ALERT_CATEGORY_ID_BIT_MASK("Alert Category ID Bit Mask", "org.bluetooth.characteristic.alert_category_id_bit_mask", 0x2A42),
        ALERT_LEVEL("Alert Level", "org.bluetooth.characteristic.alert_level", 0x2A06),
        ALERT_NOTIFICATION_CONTROL_POINT("Alert Notification Control Point", "org.bluetooth.characteristic.alert_notification_control_point", 0x2A44),
        ALERT_STATUS("Alert Status", "org.bluetooth.characteristic.alert_status", 0x2A3F),
        ALTITUDE("Altitude", "org.bluetooth.characteristic.altitude", 0x2AB3),
        ANAEROBIC_HEART_RATE_LOWER_LIMIT("Anaerobic Heart Rate Lower Limit", "org.bluetooth.characteristic.anaerobic_heart_rate_lower_limit", 0x2A81),
        ANAEROBIC_HEART_RATE_UPPER_LIMIT("Anaerobic Heart Rate Upper Limit", "org.bluetooth.characteristic.anaerobic_heart_rate_upper_limit", 0x2A82),
        ANAEROBIC_THRESHOLD("Anaerobic Threshold", "org.bluetooth.characteristic.anaerobic_threshold", 0x2A83),
        ANALOG("Analog", "org.bluetooth.characteristic.analog", 0x2A58),
        APPARENT_WIND_DIRECTION("Apparent Wind Direction", "org.bluetooth.characteristic.apparent_wind_direction", 0x2A73),
        APPARENT_WIND_SPEED("Apparent Wind Speed", "org.bluetooth.characteristic.apparent_wind_speed", 0x2A72),
        APPEARANCE("Appearance", "org.bluetooth.characteristic.gap.appearance", 0x2A01),
        BAROMETRIC_PRESSURE_TREND("Barometric Pressure Trend", "org.bluetooth.characteristic.barometric_pressure_trend", 0x2AA3),
        BATTERY_LEVEL("Battery Level", "org.bluetooth.characteristic.battery_level", 0x2A19),
        BLOOD_PRESSURE_FEATURE("Blood Pressure Feature", "org.bluetooth.characteristic.blood_pressure_feature", 0x2A49),
        BLOOD_PRESSURE_MEASUREMENT("Blood Pressure Measurement", "org.bluetooth.characteristic.blood_pressure_measurement", 0x2A35),
        BODY_COMPOSITION_FEATURE("Body Composition Feature", "org.bluetooth.characteristic.body_composition_feature", 0x2A9B),
        BODY_COMPOSITION_MEASUREMENT("Body Composition Measurement", "org.bluetooth.characteristic.body_composition_measurement", 0x2A9C),
        BODY_SENSOR_LOCATION("Body Sensor Location", "org.bluetooth.characteristic.body_sensor_location", 0x2A38),
        BOND_MANAGEMENT_CONTROL_POINT("Bond Management Control Point", "org.bluetooth.characteristic.bond_management_control_point", 0x2AA4),
        BOND_MANAGEMENT_FEATURE("Bond Management Feature", "org.bluetooth.characteristic.bond_management_feature", 0x2AA5),
        BOOT_KEYBOARD_INPUT_REPORT("Boot Keyboard Input Report", "org.bluetooth.characteristic.boot_keyboard_input_report", 0x2A22),
        BOOT_KEYBOARD_OUTPUT_REPORT("Boot Keyboard Output Report", "org.bluetooth.characteristic.boot_keyboard_output_report", 0x2A32),
        BOOT_MOUSE_INPUT_REPORT("Boot Mouse Input Report", "org.bluetooth.characteristic.boot_mouse_input_report", 0x2A33),
        CENTRAL_ADDRESS_RESOLUTION("Central Address Resolution", "org.bluetooth.characteristic.gap.central_address_resolution_support", 0x2AA6),
        CGM_FEATURE("CGM Feature", "org.bluetooth.characteristic.cgm_feature", 0x2AA8),
        CGM_MEASUREMENT("CGM Measurement", "org.bluetooth.characteristic.cgm_measurement", 0x2AA7),
        CGM_SESSION_RUN_TIME("CGM Session Run Time", "org.bluetooth.characteristic.cgm_session_run_time", 0x2AAB),
        CGM_SESSION_START_TIME("CGM Session Start Time", "org.bluetooth.characteristic.cgm_session_start_time", 0x2AAA),
        CGM_SPECIFIC_OPS_CONTROL_POINT("CGM Specific Ops Control Point", "org.bluetooth.characteristic.cgm_specific_ops_control_point", 0x2AAC),
        CGM_STATUS("CGM Status", "org.bluetooth.characteristic.cgm_status", 0x2AA9),
        CROSS_TRAINER_DATA("Cross Trainer Data", "org.bluetooth.characteristic.cross_trainer_data", 0x2ACE),
        CSC_FEATURE("CSC Feature", "org.bluetooth.characteristic.csc_feature", 0x2A5C),
        CSC_MEASUREMENT("CSC Measurement", "org.bluetooth.characteristic.csc_measurement", 0x2A5B),
        CURRENT_TIME("Current Time", "org.bluetooth.characteristic.current_time", 0x2A2B),
        CUSTOM_CHARACTERISTIC("Custom Characteristic","", 0x0000),
        CYCLING_POWER_CONTROL_POINT("Cycling Power Control Point", "org.bluetooth.characteristic.cycling_power_control_point", 0x2A66),
        CYCLING_POWER_FEATURE("Cycling Power Feature", "org.bluetooth.characteristic.cycling_power_feature", 0x2A65),
        CYCLING_POWER_MEASUREMENT("Cycling Power Measurement", "org.bluetooth.characteristic.cycling_power_measurement", 0x2A63),
        CYCLING_POWER_VECTOR("Cycling Power Vector", "org.bluetooth.characteristic.cycling_power_vector", 0x2A64),
        DATABASE_CHANGE_INCREMENT("Database Change Increment", "org.bluetooth.characteristic.database_change_increment", 0x2A99),
        DATE_OF_BIRTH("Date of Birth", "org.bluetooth.characteristic.date_of_birth", 0x2A85),
        DATE_OF_THRESHOLD_ASSESSMENT("Date of Threshold Assessment", "org.bluetooth.characteristic.date_of_threshold_assessment", 0x2A86),
        DATE_TIME("Date Time", "org.bluetooth.characteristic.date_time", 0x2A08),
        DAY_DATE_TIME("Day Date Time", "org.bluetooth.characteristic.day_date_time", 0x2A0A),
        DAY_OF_WEEK("Day of Week", "org.bluetooth.characteristic.day_of_week", 0x2A09),
        DESCRIPTOR_VALUE_CHANGED("Descriptor Value Changed", "org.bluetooth.characteristic.descriptor_value_changed", 0x2A7D),
        DEVICE_NAME("Device Name", "org.bluetooth.characteristic.gap.device_name", 0x2A00),
        DEW_POINT("Dew Point", "org.bluetooth.characteristic.dew_point", 0x2A7B),
        DIGITAL("Digital", "org.bluetooth.characteristic.digital", 0x2A56),
        DST_OFFSET("DST Offset", "org.bluetooth.characteristic.dst_offset", 0x2A0D),
        ELEVATION("Elevation", "org.bluetooth.characteristic.elevation", 0x2A6C),
        EMAIL_ADDRESS("Email Address", "org.bluetooth.characteristic.email_address", 0x2A87),
        EXACT_TIME_256("Exact Time 256", "org.bluetooth.characteristic.exact_time_256", 0x2A0C),
        FAT_BURN_HEART_RATE_LOWER_LIMIT("Fat Burn Heart Rate Lower Limit", "org.bluetooth.characteristic.fat_burn_heart_rate_lower_limit", 0x2A88),
        FAT_BURN_HEART_RATE_UPPER_LIMIT("Fat Burn Heart Rate Upper Limit", "org.bluetooth.characteristic.fat_burn_heart_rate_upper_limit", 0x2A89),
        FIRMWARE_REVISION_STRING("Firmware Revision String", "org.bluetooth.characteristic.firmware_revision_string", 0x2A26),
        FIRST_NAME("First Name", "org.bluetooth.characteristic.first_name", 0x2A8A),
        FITNESS_MACHINE_CONTROL_POINT("Fitness Machine Control Point", "org.bluetooth.characteristic.fitness_machine_control_point", 0x2AD9),
        FITNESS_MACHINE_FEATURE("Fitness Machine Feature", "org.bluetooth.characteristic.fitness_machine_feature", 0x2ACC),
        FITNESS_MACHINE_STATUS("Fitness Machine Status", "org.bluetooth.characteristic.fitness_machine_status", 0x2ADA),
        FIVE_ZONE_HEART_RATE_LIMITS("Five Zone Heart Rate Limits", "org.bluetooth.characteristic.five_zone_heart_rate_limits", 0x2A8B),
        FLOOR_NUMBER("Floor Number", "org.bluetooth.characteristic.floor_number", 0x2AB2),
        GENDER("Gender", "org.bluetooth.characteristic.gender", 0x2A8C),
        GLUCOSE_FEATURE("Glucose Feature", "org.bluetooth.characteristic.glucose_feature", 0x2A51),
        GLUCOSE_MEASUREMENT("Glucose Measurement", "org.bluetooth.characteristic.glucose_measurement", 0x2A18),
        GLUCOSE_MEASUREMENT_CONTEXT("Glucose Measurement Context", "org.bluetooth.characteristic.glucose_measurement_context", 0x2A34),
        GUST_FACTOR("Gust Factor", "org.bluetooth.characteristic.gust_factor", 0x2A74),
        HARDWARE_REVISION_STRING("Hardware Revision String", "org.bluetooth.characteristic.hardware_revision_string", 0x2A27),
        HEART_RATE_CONTROL_POINT("Heart Rate Control Point", "org.bluetooth.characteristic.heart_rate_control_point", 0x2A39),
        HEART_RATE_MAX("Heart Rate Max", "org.bluetooth.characteristic.heart_rate_max", 0x2A8D),
        HEART_RATE_MEASUREMENT("Heart Rate Measurement", "org.bluetooth.characteristic.heart_rate_measurement", 0x2A37),
        HEAT_INDEX("Heat Index", "org.bluetooth.characteristic.heat_index", 0x2A7A),
        HEIGHT("Height", "org.bluetooth.characteristic.height", 0x2A8E),
        HID_CONTROL_POINT("HID Control Point", "org.bluetooth.characteristic.hid_control_point", 0x2A4C),
        HID_INFORMATION("HID Information", "org.bluetooth.characteristic.hid_information", 0x2A4A),
        HIP_CIRCUMFERENCE("Hip Circumference", "org.bluetooth.characteristic.hip_circumference", 0x2A8F),
        HTTP_CONTROL_POINT("HTTP Control Point", "org.bluetooth.characteristic.http_control_point", 0x2ABA),
        HTTP_ENTITY_BODY("HTTP Entity Body", "org.bluetooth.characteristic.http_entity_body", 0x2AB9),
        HTTP_HEADERS("HTTP Headers", "org.bluetooth.characteristic.http_headers", 0x2AB7),
        HTTP_STATUS_CODE("HTTP Status Code", "org.bluetooth.characteristic.http_status_code", 0x2AB8),
        HTTPS_SECURITY("HTTPS Security", "org.bluetooth.characteristic.https_security", 0x2ABB),
        HUMIDITY("Humidity", "org.bluetooth.characteristic.humidity", 0x2A6F),
        IEEE_11073_20601_REGULATORY_CERTIFICATION_DATA_LIST("IEEE 11073-20601 Regulatory Certification Data List", "org.bluetooth.characteristic.ieee_11073-20601_regulatory_certification_data_list", 0x2A2A),
        INDOOR_BIKE_DATA("Indoor Bike Data", "org.bluetooth.characteristic.indoor_bike_data", 0x2AD2),
        INDOOR_POSITIONING_CONFIGURATION("Indoor Positioning Configuration", "org.bluetooth.characteristic.indoor_positioning_configuration", 0x2AAD),
        INTERMEDIATE_CUFF_PRESSURE("Intermediate Cuff Pressure", "org.bluetooth.characteristic.intermediate_cuff_pressure", 0x2A36),
        INTERMEDIATE_TEMPERATURE("Intermediate Temperature", "org.bluetooth.characteristic.intermediate_temperature", 0x2A1E),
        IRRADIANCE("Irradiance", "org.bluetooth.characteristic.irradiance", 0x2A77),
        LANGUAGE("Language", "org.bluetooth.characteristic.language", 0x2AA2),
        LAST_NAME("Last Name", "org.bluetooth.characteristic.last_name", 0x2A90),
        LATITUDE("Latitude", "org.bluetooth.characteristic.latitude", 0x2AAE),
        LN_CONTROL_POINT("LN Control Point", "org.bluetooth.characteristic.ln_control_point", 0x2A6B),
        LN_FEATURE("LN Feature", "org.bluetooth.characteristic.ln_feature", 0x2A6A),
        LOCAL_EAST_COORDINATE("Local East Coordinate", "org.bluetooth.characteristic.local_east_coordinate", 0x2AB1),
        LOCAL_NORTH_COORDINATE("Local North Coordinate", "org.bluetooth.characteristic.local_north_coordinate", 0x2AB0),
        LOCAL_TIME_INFORMATION("Local Time Information", "org.bluetooth.characteristic.local_time_information", 0x2A0F),
        LOCATION_AND_SPEED("Location and Speed", "org.bluetooth.characteristic.location_and_speed", 0x2A67),
        LOCATION_NAME("Location Name", "org.bluetooth.characteristic.location_name", 0x2AB5),
        LONGITUDE("Longitude", "org.bluetooth.characteristic.longitude", 0x2AAF),
        MAGNETIC_DECLINATION("Magnetic Declination", "org.bluetooth.characteristic.magnetic_declination", 0x2A2C),
        MAGNETIC_FLUX_DENSITY_2D("Magnetic Flux Density - 2D", "org.bluetooth.characteristic.magnetic_flux_density_2D", 0x2AA0),
        MAGNETIC_FLUX_DENSITY_3D("Magnetic Flux Density - 3D", "org.bluetooth.characteristic.magnetic_flux_density_3D", 0x2AA1),
        MANUFACTURER_NAME_STRING("Manufacturer Name String", "org.bluetooth.characteristic.manufacturer_name_string", 0x2A29),
        MAXIMUM_RECOMMENDED_HEART_RATE("Maximum Recommended Heart Rate", "org.bluetooth.characteristic.maximum_recommended_heart_rate", 0x2A91),
        MEASUREMENT_INTERVAL("Measurement Interval", "org.bluetooth.characteristic.measurement_interval", 0x2A21),
        MODEL_NUMBER_STRING("Model Number String", "org.bluetooth.characteristic.model_number_string", 0x2A24),
        NAVIGATION("Navigation", "org.bluetooth.characteristic.navigation", 0x2A68),
        NEW_ALERT("New Alert", "org.bluetooth.characteristic.new_alert", 0x2A46),
        OBJECT_ACTION_CONTROL_POINT("Object Action Control Point", "org.bluetooth.characteristic.object_action_control_point", 0x2AC5),
        OBJECT_CHANGED("Object Changed", "org.bluetooth.characteristic.object_changed", 0x2AC8),
        OBJECT_FIRST_CREATED("Object First-Created", "org.bluetooth.characteristic.object_first_created", 0x2AC1),
        OBJECT_ID("Object ID", "org.bluetooth.characteristic.object_id", 0x2AC3),
        OBJECT_LAST_MODIFIED("Object Last-Modified", "org.bluetooth.characteristic.object_last_modified", 0x2AC2),
        OBJECT_LIST_CONTROL_POINT("Object List Control Point", "org.bluetooth.characteristic.object_list_control_point", 0x2AC6),
        OBJECT_LIST_FILTER("Object List Filter", "org.bluetooth.characteristic.object_list_filter", 0x2AC7),
        OBJECT_NAME("Object Name", "org.bluetooth.characteristic.object_name", 0x2ABE),
        OBJECT_PROPERTIES("Object Properties", "org.bluetooth.characteristic.object_properties", 0x2AC4),
        OBJECT_SIZE("Object Size", "org.bluetooth.characteristic.object_size", 0x2AC0),
        OBJECT_TYPE("Object Type", "org.bluetooth.characteristic.object_type", 0x2ABF),
        OTS_FEATURE("OTS Feature", "org.bluetooth.characteristic.ots_feature", 0x2ABD),
        PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS("Peripheral Preferred Connection Parameters", "org.bluetooth.characteristic.gap.peripheral_preferred_connection_parameters", 0x2A04),
        PERIPHERAL_PRIVACY_FLAG("Peripheral Privacy Flag", "org.bluetooth.characteristic.gap.peripheral_privacy_flag", 0x2A02),
        PLX_CONTINUOUS_MEASUREMENT("PLX Continuous Measurement", "org.bluetooth.characteristic.plx_continuous_measurement", 0x2A5F),
        PLX_FEATURES("PLX Features", "org.bluetooth.characteristic.plx_features", 0x2A60),
        PLX_SPOT_CHECK_MEASUREMENT("PLX Spot-Check Measurement", "org.bluetooth.characteristic.plx_spot_check_measurement", 0x2A5E),
        PNP_ID("PnP ID", "org.bluetooth.characteristic.pnp_id", 0x2A50),
        POLLEN_CONCENTRATION("Pollen Concentration", "org.bluetooth.characteristic.pollen_concentration", 0x2A75),
        POSITION_QUALITY("Position Quality", "org.bluetooth.characteristic.position_quality", 0x2A69),
        PRESSURE("Pressure", "org.bluetooth.characteristic.pressure", 0x2A6D),
        PROTOCOL_MODE("Protocol Mode", "org.bluetooth.characteristic.protocol_mode", 0x2A4E),
        RAINFALL("Rainfall", "org.bluetooth.characteristic.rainfall", 0x2A78),
        RECONNECTION_ADDRESS("Reconnection Address", "org.bluetooth.characteristic.gap.reconnection_address", 0x2A03),
        RECORD_ACCESS_CONTROL_POINT("Record Access Control Point", "org.bluetooth.characteristic.record_access_control_point", 0x2A52),
        REFERENCE_TIME_INFORMATION("Reference Time Information", "org.bluetooth.characteristic.reference_time_information", 0x2A14),
        REPORT("Report", "org.bluetooth.characteristic.report", 0x2A4D),
        REPORT_MAP("Report Map", "org.bluetooth.characteristic.report_map", 0x2A4B),
        RESOLVABLE_PRIVATE_ADDRESS_ONLY("Resolvable Private Address Only", "org.bluetooth.characteristic.resolvable_private_address_only", 0x2AC9),
        RESTING_HEART_RATE("Resting Heart Rate", "org.bluetooth.characteristic.resting_heart_rate", 0x2A92),
        RINGER_CONTROL_POINT("Ringer Control Point", "org.bluetooth.characteristic.ringer_control_point", 0x2A40),
        RINGER_SETTING("Ringer Setting", "org.bluetooth.characteristic.ringer_setting", 0x2A41),
        ROWER_DATA("Rower Data", "org.bluetooth.characteristic.rower_data", 0x2AD1),
        RSC_FEATURE("RSC Feature", "org.bluetooth.characteristic.rsc_feature", 0x2A54),
        RSC_MEASUREMENT("RSC Measurement", "org.bluetooth.characteristic.rsc_measurement", 0x2A53),
        SC_CONTROL_POINT("SC Control Point", "org.bluetooth.characteristic.sc_control_point", 0x2A55),
        SCAN_INTERVAL_WINDOW("Scan Interval Window", "org.bluetooth.characteristic.scan_interval_window", 0x2A4F),
        SCAN_REFRESH("Scan Refresh", "org.bluetooth.characteristic.scan_refresh", 0x2A31),
        SENSOR_LOCATION("Sensor Location", "org.blueooth.characteristic.sensor_location", 0x2A5D),
        SERIAL_NUMBER_STRING("Serial Number String", "org.bluetooth.characteristic.serial_number_string", 0x2A25),
        SERVICE_CHANGED("Service Changed", "org.bluetooth.characteristic.gatt.service_changed", 0x2A05),
        SOFTWARE_REVISION_STRING("Software Revision String", "org.bluetooth.characteristic.software_revision_string", 0x2A28),
        SPORT_TYPE_FOR_AEROBIC_AND_ANAEROBIC_THRESHOLDS("Sport Type for Aerobic and Anaerobic Thresholds", "org.bluetooth.characteristic.sport_type_for_aerobic_and_anaerobic_thresholds", 0x2A93),
        STAIR_CLIMBER_DATA("Stair Climber Data", "org.bluetooth.characteristic.stair_climber_data", 0x2AD0),
        STEP_CLIMBER_DATA("Step Climber Data", "org.bluetooth.characteristic.step_climber_data", 0x2ACF),
        SUPPORTED_HEART_RATE_RANGE("Supported Heart Rate Range", "org.bluetooth.characteristic.supported_heart_rate_range", 0x2AD7),
        SUPPORTED_INCLINATION_RANGE("Supported Inclination Range", "org.bluetooth.characteristic.supported_inclination_range", 0x2AD5),
        SUPPORTED_NEW_ALERT_CATEGORY("Supported New Alert Category", "org.bluetooth.characteristic.supported_new_alert_category", 0x2A47),
        SUPPORTED_POWER_RANGE("Supported Power Range", "org.bluetooth.characteristic.supported_power_range", 0x2AD8),
        SUPPORTED_RESISTANCE_LEVEL_RANGE("Supported Resistance Level Range", "org.bluetooth.characteristic.supported_resistance_level_range", 0x2AD6),
        SUPPORTED_SPEED_RANGE("Supported Speed Range", "org.bluetooth.characteristic.supported_speed_range", 0x2AD4),
        SUPPORTED_UNREAD_ALERT_CATEGORY("Supported Unread Alert Category", "org.bluetooth.characteristic.supported_unread_alert_category", 0x2A48),
        SYSTEM_ID("System ID", "org.bluetooth.characteristic.system_id", 0x2A23),
        TDS_CONTROL_POINT("TDS Control Point", "org.bluetooth.characteristic.tds_control_point", 0x2ABC),
        TEMPERATURE("Temperature", "org.bluetooth.characteristic.temperature", 0x2A6E),
        TEMPERATURE_MEASUREMENT("Temperature Measurement", "org.bluetooth.characteristic.temperature_measurement", 0x2A1C),
        TEMPERATURE_TYPE("Temperature Type", "org.bluetooth.characteristic.temperature_type", 0x2A1D),
        THREE_ZONE_HEART_RATE_LIMITS("Three Zone Heart Rate Limits", "org.bluetooth.characteristic.three_zone_heart_rate_limits", 0x2A94),
        TIME_ACCURACY("Time Accuracy", "org.bluetooth.characteristic.time_accuracy", 0x2A12),
        TIME_SOURCE("Time Source", "org.bluetooth.characteristic.time_source", 0x2A13),
        TIME_UPDATE_CONTROL_POINT("Time Update Control Point", "org.bluetooth.characteristic.time_update_control_point", 0x2A16),
        TIME_UPDATE_STATE("Time Update State", "org.bluetooth.characteristic.time_update_state", 0x2A17),
        TIME_WITH_DST("Time with DST", "org.bluetooth.characteristic.time_with_dst", 0x2A11),
        TIME_ZONE("Time Zone", "org.bluetooth.characteristic.time_zone", 0x2A0E),
        TRAINING_STATUS("Training Status", "org.bluetooth.characteristic.training_status", 0x2AD3),
        TREADMILL_DATA("Treadmill Data", "org.bluetooth.characteristic.treadmill_data", 0x2ACD),
        TRUE_WIND_DIRECTION("True Wind Direction", "org.bluetooth.characteristic.true_wind_direction", 0x2A71),
        TRUE_WIND_SPEED("True Wind Speed", "org.bluetooth.characteristic.true_wind_speed", 0x2A70),
        TWO_ZONE_HEART_RATE_LIMIT("Two Zone Heart Rate Limit", "org.bluetooth.characteristic.two_zone_heart_rate_limit", 0x2A95),
        TX_POWER_LEVEL("Tx Power Level", "org.bluetooth.characteristic.tx_power_level", 0x2A07),
        UNCERTAINTY("Uncertainty", "org.bluetooth.characteristic.uncertainty", 0x2AB4),
        UNREAD_ALERT_STATUS("Unread Alert Status", "org.bluetooth.characteristic.unread_alert_status", 0x2A45),
        URI("URI", "org.bluetooth.characteristic.uri", 0x2AB6),
        USER_CONTROL_POINT("User Control Point", "org.bluetooth.characteristic.user_control_point", 0x2A9F),
        USER_INDEX("User Index", "org.bluetooth.characteristic.user_index", 0x2A9A),
        UV_INDEX("UV Index", "org.bluetooth.characteristic.uv_index", 0x2A76),
        VO2_MAX("VO2 Max", "org.bluetooth.characteristic.vo2_max", 0x2A96),
        WAIST_CIRCUMFERENCE("Waist Circumference", "org.bluetooth.characteristic.waist_circumference", 0x2A97),
        WEIGHT("Weight", "org.bluetooth.characteristic.weight", 0x2A98),
        WEIGHT_MEASUREMENT("Weight Measurement", "org.bluetooth.characteristic.weight_measurement", 0x2A9D),
        WEIGHT_SCALE_FEATURE("Weight Scale Feature", "org.bluetooth.characteristic.weight_scale_feature", 0x2A9E),
        WIND_CHILL("Wind Chill", "org.bluetooth.characteristic.wind_chill", 0x2A79);

    
        private final String specificationName;
        private final String specificationType;
        private final long assignedNumber;
        
        GattCharacteristics(String specificationName, String specificationType, long assignedNumber) {
            this.specificationName = specificationName;
            this.specificationType = specificationType;
            this.assignedNumber = assignedNumber;
        }

        public String getSpecificationName() {
            return specificationName;
        }

        public String getSpecificationType() {
            return specificationType;
        }

        public long getAssignedNumber() {
            return assignedNumber;
        }
        
        public static GattCharacteristics ofAssignedNumber(long number) {
            for (GattCharacteristics characteristic : values()) {
                if (characteristic.getAssignedNumber() == number) {
                    return characteristic;
                }
            }
            return GattCharacteristics.CUSTOM_CHARACTERISTIC;
        }
        
        public static GattCharacteristics ofAssignedName(String specificationName) {
            for (GattCharacteristics characteristic : values()) {
                if (characteristic.getSpecificationName().equals(specificationName)) {
                    return characteristic;
                }
            }
            return GattCharacteristics.CUSTOM_CHARACTERISTIC;
        }
    }
    
    /**
     * https://www.bluetooth.com/specifications/gatt/descriptors
     */
    public enum GattDescriptors {
        CHARACTERISTIC_AGGREGATE_FORMAT("Characteristic Aggregate Format", "org.bluetooth.descriptor.gatt.characteristic_aggregate_format", 0x2905),
        CHARACTERISTIC_EXTENDED_PROPERTIES("Characteristic Extended Properties", "org.bluetooth.descriptor.gatt.characteristic_extended_properties", 0x2900),
        CHARACTERISTIC_PRESENTATION_FORMAT("Characteristic Presentation Format", "org.bluetooth.descriptor.gatt.characteristic_presentation_format", 0x2904),
        CHARACTERISTIC_USER_DESCRIPTION("Characteristic User Description", "org.bluetooth.descriptor.gatt.characteristic_user_description", 0x2901),
        CLIENT_CHARACTERISTIC_CONFIGURATION("Client Characteristic Configuration", "org.bluetooth.descriptor.gatt.client_characteristic_configuration", 0x2902),
        ENVIRONMENTAL_SENSING_CONFIGURATION("Environmental Sensing Configuration", "org.bluetooth.descriptor.es_configuration", 0x290B),
        ENVIRONMENTAL_SENSING_MEASUREMENT("Environmental Sensing Measurement", "org.bluetooth.descriptor.es_measurement", 0x290C),
        ENVIRONMENTAL_SENSING_TRIGGER_SETTING("Environmental Sensing Trigger Setting", "org.bluetooth.descriptor.es_trigger_setting", 0x290D),
        EXTERNAL_REPORT_REFERENCE("External Report Reference", "org.bluetooth.descriptor.external_report_reference", 0x2907),
        NUMBER_OF_DIGITALS("Number of Digitals", "org.bluetooth.descriptor.number_of_digitals", 0x2909),
        REPORT_REFERENCE("Report Reference", "org.bluetooth.descriptor.report_reference", 0x2908),
        SERVER_CHARACTERISTIC_CONFIGURATION("Server Characteristic Configuration", "org.bluetooth.descriptor.gatt.server_characteristic_configuration", 0x2903),
        TIME_TRIGGER_SETTING("Time Trigger Setting", "org.bluetooth.descriptor.time_trigger_setting", 0x290E),
        VALID_RANGE("Valid Range", "org.bluetooth.descriptor.valid_range", 0x2906),
        VALUE_TRIGGER_SETTING("Value Trigger Setting", "org.bluetooth.descriptor.value_trigger_setting", 0x290A);
        
        private final String specificationName;
        private final String specificationType;
        private final long assignedNumber;
        
        GattDescriptors(String specificationName, String specificationType, long assignedNumber) {
            this.specificationName = specificationName;
            this.specificationType = specificationType;
            this.assignedNumber = assignedNumber;
        }

        public String getSpecificationName() {
            return specificationName;
        }

        public String getSpecificationType() {
            return specificationType;
        }

        public long getAssignedNumber() {
            return assignedNumber;
        }
        
        public static GattDescriptors ofAssignedNumber(long number) {
            for (GattDescriptors descriptor : values()) {
                if (descriptor.getAssignedNumber() == number) {
                    return descriptor;
                }
            }
            return GattDescriptors.CHARACTERISTIC_EXTENDED_PROPERTIES;
        }
        
        public static GattDescriptors ofAssignedName(String specificationName) {
            for (GattDescriptors descriptor : values()) {
                if (descriptor.getSpecificationName().equals(specificationName)) {
                    return descriptor;
                }
            }
            return GattDescriptors.CHARACTERISTIC_EXTENDED_PROPERTIES;
        }
    }
    
    public static String getServiceToken(UUID serviceUuid) {
        final long assignedNumber = getAssignedNumber(serviceUuid);
        if (GattServices.ofAssignedNumber(assignedNumber).equals(GattServices.CUSTOM_SERVICE)) {
            return serviceUuid.toString();
        }
        return formatToken(assignedNumber);
    }

    public static String getCharacteristicsToken(UUID characteristicsUuid) {
        final long assignedNumber = getAssignedNumber(characteristicsUuid);
        if (GattCharacteristics.ofAssignedNumber(assignedNumber).equals(GattCharacteristics.CUSTOM_CHARACTERISTIC)) {
            return characteristicsUuid.toString();
        }
        return formatToken(assignedNumber);
    }

    public static UUID getUUIDFromServiceName(String serviceName) {
        GattServices gattService = GattServices.ofAssignedName(serviceName);
        if (gattService.equals(GattServices.CUSTOM_SERVICE)) {
            return getUUIDfromToken(serviceName);
        }
        final String serviceToken = formatToken(gattService.getAssignedNumber());
        return getUUIDfromToken(serviceToken);
    }

    public static UUID getUUIDFromCharacteristicsName(String charName) {
        GattCharacteristics gattChars = GattCharacteristics.ofAssignedName(charName);
        if (gattChars.equals(GattCharacteristics.CUSTOM_CHARACTERISTIC)) {
            return getUUIDfromToken(charName);
        }
        final String charToken = formatToken(gattChars.getAssignedNumber());
        return getUUIDfromToken(charToken);
    }

    public static UUID getUUIDFromDescriptorName(String descName) {
        GattDescriptors gattDescriptor = GattDescriptors.ofAssignedName(descName);
        final String descToken = formatToken(gattDescriptor.getAssignedNumber());
        return getUUIDfromToken(descToken);
    }

    public static Optional<UUID> getUUIDfromTokenOrElse(String token, Function<String, UUID> functionName) {
        UUID uuid = getUUIDfromToken(token);
        if (uuid == null && functionName != null) {
            uuid = functionName.apply(token);
        }
        return uuid == null ? Optional.empty() : Optional.of(uuid);
    }

    public static String formatToken(long assignedNumber) {
        return String.format("%04x", assignedNumber);
    }

    public static long getAssignedNumber(UUID uuid) {
        return (uuid.getMostSignificantBits() & 0x0000FFFF00000000L) >> 32;
    }

    private static UUID getUUIDfromToken(String value) {
        if (value == null || value.isEmpty()) {
            LOG.log(Level.INFO, "Error retrieving UUID for " + value);
            return null;
        }
        try {
            if (value.length() == 4) {
                return UUID.fromString(BleSpecs.UUID_BASE_PREFIX + value + BleSpecs.UUID_BASE_SUFIX);
            }
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Error retrieving UUID for " + value + ": " + e.getMessage());
        }

        return null;
    }
}
