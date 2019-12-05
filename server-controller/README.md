 # Broadlink Bridge
 - [Introductions](#introdution)
 - [Requirements](#requirements)
 - [Installation](#installation)
 ## Introductions
 Một project được phát triển từ [Broadlink MQTT Bridge](https://github.com/fbacker/broadlink-mqtt-bridge) để thực hiện giao tiếp, quản lý các thiết bị broadlink như `Broadlink RM Mini 3`, `Broadlink RM3 Pro Plus`, ... bao gồm các chức năng: 
 - Tìm kiếm thiết bị broadlink trong mạng
 - Hiển thị danh sách thiết bị broadlink đã kết nối
 - Điều khiển Broadlink học lệnh IR/RF
 - Hiển thị các lệnh đã học
 - Thực hiện chạy lệnh đã học
 
 Thông qua các cách giao tiếp sau:
 - MQTT: Thực hiện Giao tiếp với thiết bị Broadlink qua việc publish/subscriber tới các topic (sau này có thể sử dụng topic là đường link cho openHAB)
 - Web server: Giao tiếp với thiết bị Broadlink qua giao diện người dùng
 - Rest API: Giao tiếp với thiết bị Broadlink qua HTTP request tới các API
 ## Requirements

- [Nodejs](https://nodejs.org/en/) > 8 
- MQTT ([mosquitto](https://mosquitto.org/) hoặc các MQTT broker khác )
- Thiết bị Broadlink e.g. RM 3 mini 3 ...

## Installation
```sh
npm install 
```

## Configure
Cấu hình được đặt trong `./config/default.json`, bạn có thể thay đổi cấu hình mqtt, web server và cấu hình thiết bị broadlink


## Topic
- Publish bản tin tới topic này để lấy thông tin các thiết bị broadlink hiện có trong mạng 
```js
REQUEST_DEVICE_INFO_TOPIC = "broadlink/airpurifier/info";
payload = "setpower-<Smart Plug ID>"
```
- Kết quả trả về được publish lên:
```js
REQUEST_DEVICE_INFO_TOPIC = "broadlink-stat/airpurifier/info"; 
/*     [{"name": "device_name_1", "id": "device_id_2"}, 
        {"name": "device_name_1", "id": "device_id_2"}, ...]
*/
```

- Các Topic để gửi lệnh IR cho thiết bị
```js
POWER_TOPIC = "broadlink/airpurifier/power" 
payload = "play-<RM mini 3 ID>"
...
LOW_SPEED_TOPIC = "broadlink/airpurifier/low" 
payload = "play-<RM mini 3 ID>"
...
MED_SPEED_TOPIC = "broadlink/airpurifier/med" 
payload = "play-<RM mini 3 ID>"
...
HIGH_SPEED_TOPIC = "broadlink/airpurifier/high" 
payload = "play-<RM mini 3 ID>"
```
- Gửi bản tin tới các topic này để gửi yêu cầu lấy trạng thái thiết bị, kết quả trả về ở Topic chứa trạng thái bị
```js
REQUEST_STATE_POWER_TOPIC = "broadlink/airpurifier/info";
payload = "checkpower-<Smart Plug ID>"
...
REQUEST_STATE_SPEED_TOPIC = "broadlink/airpurifier/info";
payload = "checkspeed-<Smart Plug ID>"
```

- Các Topic chứa trạng thái thiết bị - kết quả trả về sau khi yêu cầu lấy trạng thái thiết bị
```js
STATE_POWER_TOPIC = "broadlink-stat/airpurifier/power";  // ON - OFF
STATE_SPEED_TOPIC = "broadlink-stat/airpurifier/speed";    // 0 - 1 - 2 - 3
```
- Gửi bản tin tới các topic này để yêu cầu bật thiết bị smart plug
```js
SET_STATE_POWER_TOPIC = "broadlink/airpurifier/info";
payload = "setpower-<Smart Plug ID>"
```
- Topic để lấy/set chế độ chạy cho service (auto/manual)
```js
REQUEST_GET_CURRENT_MODE_TOPIC = "broadlink/airthinx/getcurrentmode";
payload = "getairthinxmode"
REQUEST_SET_CURRENT_MODE_TOPIC = "broadlink/airthinx/setmode";
payload = "setairthinxmode-<auto/manual>"

RESPONSE_CURRENT_MODE_TOPIC="broadlink-stat/airthinx/currentmode";
response = auto/manual
```